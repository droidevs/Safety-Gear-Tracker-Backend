package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.SafetyViolation;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageOverlayServiceImpl implements ImageOverlayService {

    @Override
    public byte[] drawViolationsOnImage(byte[] originalImage, List<SafetyViolation> violations) {
        Mat image = Imgcodecs.imdecode(new MatOfByte(originalImage), Imgcodecs.IMREAD_COLOR);

        for (SafetyViolation violation : violations) {
            // Draw a red bounding box around the person
            Rect box = new Rect(
                violation.personBoundingBox().x(),
                violation.personBoundingBox().y(),
                violation.personBoundingBox().width(),
                violation.personBoundingBox().height()
            );
            Imgproc.rectangle(image, box, new Scalar(0, 0, 255), 2); // Red color, thickness 2

            // Create a label with the missing gear
            String label = "Missing: " + violation.missingGear().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

            // Put the label above the bounding box
            Point labelPosition = new Point(box.x, box.y - 10);
            Imgproc.putText(image, label, labelPosition, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte);
        return matOfByte.toArray();
    }
}
