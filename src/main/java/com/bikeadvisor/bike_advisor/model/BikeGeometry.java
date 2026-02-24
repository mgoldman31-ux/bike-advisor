package com.bikeadvisor.bike_advisor.model;

public class BikeGeometry {
    private String bikeGeometryKey;  // Composition: Geometry belongs to a bike
    private String sizeLabel;      // e.g. "54", "M", "L"

    // all lengths in mm
    private Double reach;
    private Double stack;
    private Double topTubeEffective;
    private Double headTubeAngle;
    private Double seatTubeAngleEffective;
    private Double chainstay;
    private Double wheelbase;
    private Double bbDrop;
    private Double forkOffset;
    private Double headTubeLength;
    private Double seatTubeLength;
    private Double standover;
    private Double trail;
    private String wheelSize;

    public void setBikeGeometryKey(String bikeGeometryKey) { this.bikeGeometryKey = bikeGeometryKey; }
    public String getBikeGeometryKey() { return bikeGeometryKey; }
    public void setSizeLabel(String sizeLabel) { this.sizeLabel = sizeLabel; }
    public String getSizeLabel() { return sizeLabel; }
    public void setReach(Double reach) { this.reach = reach; }
    public Double getReach() { return reach; }
    public void setStack(Double stack) { this.stack = stack; }
    public Double getStack() { return stack; }
    public Double getTopTubeEffective() { return topTubeEffective; }
    public void setTopTubeEffective(Double topTubeEffective) { this.topTubeEffective = topTubeEffective; }
    public Double getHeadTubeAngle() { return headTubeAngle; }
    public void setHeadTubeAngle(Double headTubeAngle) { this.headTubeAngle = headTubeAngle; }
    public Double getSeatTubeAngleEffective() { return seatTubeAngleEffective; }
    public void setSeatTubeAngleEffective(Double seatTubeEffective) { this.seatTubeAngleEffective = seatTubeEffective; }
    public Double getChainstay() { return chainstay; }
    public void setChainstay(Double chainstay) { this.chainstay = chainstay; }
    public Double getWheelbase() { return wheelbase; }
    public void setWheelbase(Double wheelbase) { this.wheelbase = wheelbase; }
    public Double getBbDrop() { return bbDrop; }
    public void setBbDrop(Double bbDrop) { this.bbDrop = bbDrop; }
    public Double getForkOffset() { return forkOffset; }
    public void setForkOffset(Double forkOffset) { this.forkOffset = forkOffset; }
    public Double getHeadTubeLength() { return headTubeLength; }
    public void setHeadTubeLength(Double headTubeLength) { this.headTubeLength = headTubeLength; }
    public Double getSeatTubeLength() { return seatTubeLength; }
    public void setSeatTubeLength(Double seatTubeLength) { this.seatTubeLength = seatTubeLength; }
    public Double getStandover() { return standover; }
    public void setStandover(Double standover) { this.standover = standover; }
    public Double getTrail() { return trail; }
    public void setTrail(Double trail) { this.trail = trail; }
    public String getWheelSize() { return wheelSize; }
    public void setWheelSize(String wheelSize) { this.wheelSize = wheelSize; }

}
