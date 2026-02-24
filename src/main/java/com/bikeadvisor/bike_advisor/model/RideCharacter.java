package com.bikeadvisor.bike_advisor.model;

public class RideCharacter {
    // Primary keys for the geometry data
    private String geometryKey;
    private String sizeLabel;

    // Physical index values normalized with min/max scaling
    private double stabilityIndex;
    private double agilityIndex; // opposite of stability
    private double comfortIndex;
    private double aggressionIndex; // opposite of comfort, includes head angle
    private double handlingIndex; // trail and head angle

    // Z-score index values
    private double stabilityZIndex;
    private double agilityZIndex;
    private double comfortZIndex;
    private double aggressionZIndex;
    private double handlingZIndex;

    public RideCharacter(String geometryKey, String sizeLabel) {
        this.geometryKey = geometryKey;
        this.sizeLabel = sizeLabel;
    }

    /* Getters / Setters */
    public String getGeometryKey() { return this.geometryKey; }
    public String getSizeLabel() { return this.sizeLabel; }

    public double getStabilityIndex() {
        return stabilityIndex;
    }

    public void setStabilityIndex(double stabilityIndex) {
        this.stabilityIndex = stabilityIndex;
    }

    public double getComfortIndex() {
        return comfortIndex;
    }

    public void setComfortIndex(double comfortIndex) { this.comfortIndex = comfortIndex; }

    public double getAgilityIndex() { return agilityIndex; }

    public void setAgilityIndex(double agilityIndex) {
        this.agilityIndex = agilityIndex;
    }

    public double getAggressionIndex() {
        return aggressionIndex;
    }

    public void setAggressionIndex(double aggressionIndex) {
        this.aggressionIndex = aggressionIndex;
    }

    public double getHandlingIndex() {
        return handlingIndex;
    }

    public void setHandlingIndex(double handlingIndex) {
        this.handlingIndex = handlingIndex;
    }

    // Z-score index value getters/setters
    public double getStabilityZIndex() { return stabilityZIndex; }
    public void setStabilityZIndex(double stabilityZIndex) { this.stabilityZIndex = stabilityZIndex; }
    public double getAgilityZIndex() { return agilityZIndex; }
    public void setAgilityZIndex(double agilityZIndex) { this.agilityZIndex = agilityZIndex; }
    public double getComfortZIndex() { return comfortZIndex; }
    public void setComfortZIndex(double comfortZIndex) {this.comfortZIndex = comfortZIndex; }
    public double getAggressionZIndex() { return aggressionZIndex; }
    public void setAggressionZIndex(double aggressionZIndex) { this.aggressionZIndex = aggressionZIndex; }
    public double getHandlingZIndex() { return handlingZIndex; }
    public void setHandlingZIndex(double handlingZIndex) {this.handlingZIndex = handlingZIndex; }
}
