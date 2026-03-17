package com.bikeadvisor.bike_advisor.dto;

public record BikeDetailRow(
        String sizeLabel,
        Double reach,
        Double stack,
        Double wheelbase,
        Double headTubeAngle,
        Double chainstay,
        Double bbDrop,
        Double trail,
        Double stabilityIndex,
        Double aeroIndex,
        Double agilityIndex
) {}
