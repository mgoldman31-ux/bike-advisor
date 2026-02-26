package com.bikeadvisor.bike_advisor.dto;

import java.util.List;

public record FilterOptions(
        List<String> brands,
        List<String> disciplines,
        List<String> wheelSizes
) {}
