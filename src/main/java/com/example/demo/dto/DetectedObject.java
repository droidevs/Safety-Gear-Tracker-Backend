
package com.example.demo.dto;

import com.example.demo.dto.BoundingBox;

public class DetectedObject {

    private String label;
    private BoundingBox boundingBox;
    private double confidence;

    public DetectedObject(String label, BoundingBox boundingBox, double confidence) {
        this.label = label;
        this.boundingBox = boundingBox;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
