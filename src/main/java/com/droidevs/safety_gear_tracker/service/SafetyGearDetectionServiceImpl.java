package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.BoundingBox;
import com.droidevs.safety_gear_tracker.dto.SafetyViolation;
import com.droidevs.safety_gear_tracker.handler.exception.GlobalBaseException;
import com.droidevs.safety_gear_tracker.handler.exception.JsonParsingException;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyGearDetectionServiceImpl implements SafetyGearDetectionService {

    private final GenerativeModel generativeModel;

    @Override
    public List<SafetyViolation> findViolations(byte[] imageData, Camera camera) {
        if (camera.getRequiredSafetyGear() == null || camera.getRequiredSafetyGear().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return processFrameWithAI(imageData, camera);
        } catch (IOException | GlobalBaseException e) { // Corrected multi-catch statement
            log.error("Error during AI frame processing for camera {}: {}", camera.getId(), e.getMessage(), e);
            // Since this method is called within VideoProcessor (which is run by taskExecutor),
            // re-throwing the exception will allow CustomTaskErrorHandler to catch it.
            // For now, returning emptyList to prevent stopping the entire video stream on a single error.
            // Depending on desired error propagation, this could be re-thrown.
            return Collections.emptyList();
        }
    }

    private List<SafetyViolation> processFrameWithAI(byte[] imageData, Camera camera) throws IOException, JsonParsingException {
        String requiredGearList = camera.getRequiredSafetyGear().stream()
                .map(gear -> "\"" + gear.name().toLowerCase().replace("_", " ") + "\"")
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "You are a safety inspector. Analyze the image and identify each person. " +
                "For each person, check if they are wearing all of the following required safety gear: [%s]. " +
                "Return a JSON array named 'violations' where each object represents a person who is missing one or more items. " +
                "Each object in the array should have: " +
                "1. A 'person_bounding_box' [x, y, width, height]. " +
                "2. A 'missing_gear' array of strings listing the names of the gear they are not wearing. " +
                "If no violations are found, return an empty 'violations' array.",
                requiredGearList
        );

        GenerateContentResponse response = generativeModel.generateContent(
                ContentMaker.fromMultiModalData(
                        PartMaker.fromMimeTypeAndData("image/jpeg", imageData),
                        prompt
                )
        );
        String textResponse = ResponseHandler.getText(response);
        return parseViolationsJsonResponse(textResponse, camera.getId()); // Pass camera.getId()
    }

    private List<SafetyViolation> parseViolationsJsonResponse(String jsonResponse, Long cameraId) throws JsonParsingException { // Added cameraId parameter
        String cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim();
        List<SafetyViolation> violations = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(cleanedJson);
            JSONArray violationsArray = root.getJSONArray("violations");

            for (int i = 0; i < violationsArray.length(); i++) {
                JSONObject obj = violationsArray.getJSONObject(i);
                JSONArray box = obj.getJSONArray("person_bounding_box");
                BoundingBox boundingBox = new BoundingBox(box.getInt(0), box.getInt(1), box.getInt(2), box.getInt(3));

                JSONArray missingGearArray = obj.getJSONArray("missing_gear");
                List<SafetyGearType> missingGear = new ArrayList<>();
                for (int j = 0; j < missingGearArray.length(); j++) {
                    try {
                        String gearString = missingGearArray.getString(j).toUpperCase().replace(" ", "_");
                        missingGear.add(SafetyGearType.valueOf(gearString));
                    } catch (IllegalArgumentException e) {
                        log.warn("Model returned unknown safety gear type for camera {}: {}", cameraId, missingGearArray.getString(j)); // Use cameraId
                        // For now, we'll just log and skip unknown gear types.
                    }
                }

                if (!missingGear.isEmpty()) {
                    violations.add(new SafetyViolation(boundingBox, missingGear));
                }
            }
        } catch (org.json.JSONException e) {
            throw new JsonParsingException("Error parsing violations JSON response", e);
        }
        return violations;
    }
}
