package com.bikeadvisor.bike_advisor.dto;

public record ScatterPoint(
        String bikeGeometryKey,
        String sizeLabel,
        Double stack,
        Double reach,
        String wheelSize,
        int bikeCount
) {}
