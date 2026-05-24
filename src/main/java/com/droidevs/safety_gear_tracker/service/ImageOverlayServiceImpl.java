package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.SafetyViolation;
import com.droidevs.safety_gear_tracker.handler.exception.ImageProcessingException;
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

/**
 * BUG-06 FIX: The original method was annotated @Async but returned byte[].
 * Spring's async proxy requires void or Future<T> return types — returning
 * a concrete type from an @Async method causes IllegalStateException at runtime.
 * Fix: remove @Async. The VideoProcessor calls this synchronously already.
 */
@Service
public class ImageOverlayServiceImpl implements ImageOverlayService {

    @Override
    public byte[] drawViolationsOnImage(byte[] originalImage, List<SafetyViolation> violations) {
        Mat image;
        try {
            image = Imgcodecs.imdecode(new MatOfByte(originalImage), Imgcodecs.IMREAD_COLOR);
            if (image.empty()) {
                throw new ImageProcessingException("Could not decode image or image is empty.");
            }
        } catch (ImageProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ImageProcessingException("Failed to decode original image for overlay.", e);
        }

        for (SafetyViolation violation : violations) {
            Rect box = new Rect(
                    violation.personBoundingBox().x(),
                    violation.personBoundingBox().y(),
                    violation.personBoundingBox().width(),
                    violation.personBoundingBox().height()
            );
            Imgproc.rectangle(image, box, new Scalar(0, 0, 255), 2);

            String label = "Missing: " + violation.missingGear().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            Point labelPosition = new Point(box.x, Math.max(0, box.y - 10));
            Imgproc.putText(image, label, labelPosition, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
        }

        MatOfByte matOfByte = new MatOfByte();
        try {
            Imgcodecs.imencode(".jpg", image, matOfByte);
        } catch (Exception e) {
            throw new ImageProcessingException("Failed to encode image with violations.", e);
        }
        return matOfByte.toArray();
    }
}