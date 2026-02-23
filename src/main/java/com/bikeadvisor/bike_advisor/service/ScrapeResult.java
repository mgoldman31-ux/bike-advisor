package com.bikeadvisor.bike_advisor.service;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;

import java.util.List;
import java.util.Map;

/**
 * The output of a scrape run.
 *
 * bikes            — all BikeSummaries across all brands, first calculated for individual brands
 * geometriesByKey  — deduplicated geometry rows keyed by geometryKey; each key maps to one
 *                    set of size rows, regardless of how many bikes share the geometry
 */
public record ScrapeResult(
        List<BikeSummary> bikes,
        Map<String, List<BikeGeometry>> geometriesByKey
) {}
