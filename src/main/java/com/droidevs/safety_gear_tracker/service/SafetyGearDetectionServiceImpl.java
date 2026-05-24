package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.BoundingBox;
import com.droidevs.safety_gear_tracker.dto.SafetyViolation;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SafetyGearDetectionServiceImpl implements SafetyGearDetectionService {

    @Value("${yolo.weights.path:}")
    private String yoloWeightsPath;

    @Value("${yolo.cfg.path:}")
    private String yoloCfgPath;

    @Value("${yolo.classes.path:}")
    private String yoloClassesPath;

    private org.opencv.dnn.Net net;
    private List<String> outNames;
    private List<String> classes;

    @PostConstruct
    public void initYolo() {
        if (isYoloConfigured()) {
            try {
                net = org.opencv.dnn.Dnn.readNetFromDarknet(yoloCfgPath, yoloWeightsPath);
                outNames = net.getUnconnectedOutLayersNames();
                classes = new ArrayList<>();
                if (yoloClassesPath != null && !yoloClassesPath.isBlank()) {
                    Path path = Paths.get(yoloClassesPath);
                    if (Files.exists(path)) {
                        classes = Files.readAllLines(path);
                    }
                }
                log.info("YOLO Net successfully loaded from {} and {}", yoloCfgPath, yoloWeightsPath);
            } catch (Exception e) {
                log.error("Failed to load YOLO model, falling back to Simulation mode. Error: {}", e.getMessage(), e);
                net = null;
            }
        } else {
            log.info("YOLO model not configured or files not found. SafetyGearDetectionService will run in high-fidelity SIMULATION mode.");
        }
    }

    private boolean isYoloConfigured() {
        return yoloWeightsPath != null && !yoloWeightsPath.isBlank() &&
               yoloCfgPath != null && !yoloCfgPath.isBlank() &&
               new File(yoloWeightsPath).exists() &&
               new File(yoloCfgPath).exists();
    }

    @Override
    public List<SafetyViolation> findViolations(byte[] imageData, Camera camera) {
        if (camera.getRequiredSafetyGear() == null || camera.getRequiredSafetyGear().isEmpty()) {
            return Collections.emptyList();
        }

        if (net != null) {
            return runYoloDetection(imageData, camera);
        } else {
            return getSimulatedViolations(camera);
        }
    }

    private List<SafetyViolation> runYoloDetection(byte[] imageData, Camera camera) {
        MatOfByte mob = new MatOfByte(imageData);
        Mat frame = null;
        Mat blob = null;
        List<Mat> outs = new ArrayList<>();
        try {
            frame = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
            if (frame.empty()) {
                return Collections.emptyList();
            }

            blob = org.opencv.dnn.Dnn.blobFromImage(frame, 1.0 / 255.0, new org.opencv.core.Size(416, 416), new org.opencv.core.Scalar(0, 0, 0), true, false);
            net.setInput(blob);
            net.forward(outs, outNames);

            List<Rect> persons = new ArrayList<>();
            List<Rect> helmets = new ArrayList<>();
            List<Rect> vests = new ArrayList<>();

            float confThreshold = 0.5f;
            for (Mat out : outs) {
                for (int i = 0; i < out.rows(); i++) {
                    double confidence = out.get(i, 4)[0];
                    if (confidence > confThreshold) {
                        int classId = -1;
                        double maxProb = 0.0;
                        for (int j = 5; j < out.cols(); j++) {
                            double prob = out.get(i, j)[0];
                            if (prob > maxProb) {
                                maxProb = prob;
                                classId = j - 5;
                            }
                        }

                        if (maxProb > confThreshold) {
                            int centerX = (int) (out.get(i, 0)[0] * frame.cols());
                            int centerY = (int) (out.get(i, 1)[0] * frame.rows());
                            int width = (int) (out.get(i, 2)[0] * frame.cols());
                            int height = (int) (out.get(i, 3)[0] * frame.rows());
                            int left = centerX - width / 2;
                            int top = centerY - height / 2;

                            Rect rect = new Rect(left, top, width, height);
                            String className = getClassName(classId);
                            if ("person".equalsIgnoreCase(className)) {
                                persons.add(rect);
                            } else if ("helmet".equalsIgnoreCase(className) || "hard hat".equalsIgnoreCase(className)) {
                                helmets.add(rect);
                            } else if ("vest".equalsIgnoreCase(className) || "safety vest".equalsIgnoreCase(className)) {
                                vests.add(rect);
                            }
                        }
                    }
                }
            }

            List<SafetyViolation> violations = new ArrayList<>();
            List<SafetyGearType> required = camera.getRequiredSafetyGear().stream().toList();

            for (Rect person : persons) {
                List<SafetyGearType> missing = new ArrayList<>();
                if (required.contains(SafetyGearType.HARD_HAT)) {
                    boolean hasHelmet = false;
                    for (Rect helmet : helmets) {
                        if (intersects(person, helmet)) {
                            hasHelmet = true;
                            break;
                        }
                    }
                    if (!hasHelmet) {
                        missing.add(SafetyGearType.HARD_HAT);
                    }
                }

                if (required.contains(SafetyGearType.SAFETY_VEST)) {
                    boolean hasVest = false;
                    for (Rect vest : vests) {
                        if (intersects(person, vest)) {
                            hasVest = true;
                            break;
                        }
                    }
                    if (!hasVest) {
                        missing.add(SafetyGearType.SAFETY_VEST);
                    }
                }

                if (!missing.isEmpty()) {
                    violations.add(new SafetyViolation(
                        new BoundingBox(person.x, person.y, person.width, person.height),
                        missing
                    ));
                }
            }

            return violations;
        } catch (Exception e) {
            log.error("Error during local YOLO detection: {}", e.getMessage(), e);
            return getSimulatedViolations(camera);
        } finally {
            mob.release();
            if (frame != null) {
                frame.release();
            }
            if (blob != null) {
                blob.release();
            }
            for (Mat m : outs) {
                m.release();
            }
        }
    }

    private List<SafetyViolation> getSimulatedViolations(Camera camera) {
        List<SafetyGearType> required = camera.getRequiredSafetyGear().stream().toList();
        if (required == null || required.isEmpty()) {
            return Collections.emptyList();
        }
        
        long sec = java.time.Instant.now().getEpochSecond();
        if (sec % 15 != 0) {
            return Collections.emptyList();
        }

        log.info("Simulation mode: generating mock safety violation for camera {}", camera.getId());
        BoundingBox personBox = new BoundingBox(100, 100, 200, 400);
        List<SafetyGearType> missing = new ArrayList<>();
        missing.add(required.get(0));
        
        return List.of(new SafetyViolation(personBox, missing));
    }

    private String getClassName(int classId) {
        if (classes != null && classId >= 0 && classId < classes.size()) {
            return classes.get(classId);
        }
        return classId == 0 ? "person" : "unknown";
    }

    private boolean intersects(Rect r1, Rect r2) {
        int x1 = Math.max(r1.x, r2.x);
        int y1 = Math.max(r1.y, r2.y);
        int x2 = Math.min(r1.x + r1.width, r2.x + r2.width);
        int y2 = Math.min(r1.y + r1.height, r2.y + r2.height);
        return x1 < x2 && y1 < y2;
    }
}
