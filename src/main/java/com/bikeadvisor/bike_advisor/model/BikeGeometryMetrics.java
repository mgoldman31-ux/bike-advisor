package com.bikeadvisor.bike_advisor.model;

public class BikeGeometryMetrics {
    // Geometry identifier
    private String geometryKey;

    // Front / rear distribution
    private double frontCenter;
    private double rearCenter;

    // Ratios
    private double stackReachRatio;
    private double frontCenterRatio;
    private double rearCenterRatio;

    // Steering-related
    private double selfCentering;

    public BikeGeometryMetrics(String geometryKey) {
        this.geometryKey = geometryKey;
    }

    /* Getters / Setters */

    public String getGeometryKey() {
        return geometryKey;
    }

    public double getFrontCenter() {
        return frontCenter;
    }

    public void setFrontCenter(double frontCenter) {
        this.frontCenter = frontCenter;
    }

    public double getRearCenter() {
        return rearCenter;
    }

    public void setRearCenter(double rearCenter) {
        this.rearCenter = rearCenter;
    }

    public double getStackReachRatio() {
        return stackReachRatio;
    }

    public void setStackReachRatio(double stackReachRatio) {
        this.stackReachRatio = stackReachRatio;
    }

    public double getFrontCenterRatio() {
        return frontCenterRatio;
    }

    public void setFrontCenterRatio(double frontCenterRatio) {
        this.frontCenterRatio = frontCenterRatio;
    }

    public double getRearCenterRatio() {
        return rearCenterRatio;
    }

    public void setRearCenterRatio(double rearCenterRatio) {
        this.rearCenterRatio = rearCenterRatio;
    }

    public double getSelfCentering() {
        return selfCentering;
    }

    public void setSelfCentering(double selfCentering) {
        this.selfCentering = selfCentering;
    }
}
