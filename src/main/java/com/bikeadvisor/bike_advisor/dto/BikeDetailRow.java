package com.bikeadvisor.bike_advisor.dto;

public record BikeDetailRow(
        String sizeLabel,
        String wheelSize,
        Double reach,
        Double stack,
        Double topTubeEffective,
        Double headTubeAngle,
        Double seatTubeAngleEffective,
        Double headTubeLength,
        Double seatTubeLength,
        Double standover,
        Double wheelbase,
        Double chainstay,
        Double bbDrop,
        Double forkOffset,
        Double trail,
        Double stabilityIndex,
        Double aeroIndex,
        Double agilityIndex
) {}
