package com.bikeadvisor.bike_advisor.model;

public class RideCharacter {
    private String geometryKey;
    private String sizeLabel;
    private Integer sizeBucket;

    private double stabilityIndex;
    private double aeroIndex;
    private double agilityIndex;

    public RideCharacter(String geometryKey, String sizeLabel) {
        this.geometryKey = geometryKey;
        this.sizeLabel = sizeLabel;
    }

    /* Getters / Setters */
    public String getGeometryKey() { return geometryKey; }
    public String getSizeLabel() { return sizeLabel; }
    public Integer getSizeBucket() { return sizeBucket; }
    public void setSizeBucket(Integer sizeBucket) { this.sizeBucket = sizeBucket; }

    public double getStabilityIndex() { return stabilityIndex; }
    public void setStabilityIndex(double stabilityIndex) { this.stabilityIndex = stabilityIndex; }

    public double getAeroIndex() { return aeroIndex; }
    public void setAeroIndex(double aeroIndex) { this.aeroIndex = aeroIndex; }

    public double getAgilityIndex() { return agilityIndex; }
    public void setAgilityIndex(double agilityIndex) { this.agilityIndex = agilityIndex; }
}
