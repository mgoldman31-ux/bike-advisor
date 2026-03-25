package com.bikeadvisor.bike_advisor.dto;

public record ScatterPoint(
        String bikeGeometryKey,
        String sizeLabel,
        String brand,
        String model,
        Double stabilityIndex,
        Double aeroIndex,
        String wheelSize,
        int bikeCount
) {}
