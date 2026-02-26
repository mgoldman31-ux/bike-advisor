package com.bikeadvisor.bike_advisor.service;

import com.bikeadvisor.bike_advisor.model.GeometryMetricsStats;
import com.bikeadvisor.bike_advisor.model.GeometryMetricKey;
import org.springframework.stereotype.Service;

import com.bikeadvisor.bike_advisor.model.RideCharacter;
import com.bikeadvisor.bike_advisor.model.BikeGeometry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service

public class GeometryMetricsService {

    // Note that true rolling radius includes half of tire height, though this is fine for comparisons since error is small and consistent
    private static final Map<String, Double> WHEEL_RADIUS_MM = Map.of(
            "700c", 311.0,   // ISO 622mm BSD
            "650b", 292.0,   // ISO 584mm BSD
            "650c", 285.5,   // ISO 571mm BSD
            "29",   311.0,   // same rim as 700c
            "27.5", 292.0,   // same rim as 650b
            "26",   279.5    // ISO 559mm BSD
    );

    // Computes min, max, mean, stdev for primary geometry metrics
    public Map<GeometryMetricKey, GeometryMetricsStats> computeStats(List<BikeGeometry> geometries) {
        Map<GeometryMetricKey, List<Double>> statValuesByMetric = new EnumMap<>(GeometryMetricKey.class);

        // for each geometry object in the input list, add its values to the list of all values in statValuesByMetric
        for (BikeGeometry g : geometries) {
            addGeometryValue(statValuesByMetric, GeometryMetricKey.TRAIL, g.getTrail());
            addGeometryValue(statValuesByMetric, GeometryMetricKey.WHEELBASE, g.getWheelbase());
            addGeometryValue(statValuesByMetric, GeometryMetricKey.HEAD_ANGLE, g.getHeadTubeAngle());
            addGeometryValue(statValuesByMetric, GeometryMetricKey.REACH, g.getReach());
            addGeometryValue(statValuesByMetric, GeometryMetricKey.STACK, g.getStack());
            addGeometryValue(statValuesByMetric, GeometryMetricKey.CHAINSTAY, g.getChainstay());
            addGeometryValue(statValuesByMetric, GeometryMetricKey.SEAT_TUBE_LENGTH, g.getSeatTubeLength());
        }

        // Maps the geometry metric name (trail, wheelbase, etc.) to its metrics stats object (min, max, stdev, etc.)
        Map<GeometryMetricKey, GeometryMetricsStats> allMetricsStats = new EnumMap<>(GeometryMetricKey.class);

        // for each geometry metric, compute its GeometryMetricsStats and add to the stats map to be returned
        for (Map.Entry<GeometryMetricKey, List<Double>> entry: statValuesByMetric.entrySet()) {
            GeometryMetricsStats currMetricStat = computeMetricsStats(entry.getValue());
            allMetricsStats.put(entry.getKey(), currMetricStat);
        }

        return allMetricsStats;
    }

    public void deriveTrailWhereMissing(List<BikeGeometry> geometries) {
        for (BikeGeometry g : geometries) {
            if (g.getTrail() != null) continue;
            Double radius = g.getWheelSize() != null ? WHEEL_RADIUS_MM.get(g.getWheelSize().toLowerCase()) : null;
            if (radius == null || g.getHeadTubeAngle() == null || g.getForkOffset() == null) continue;
            double htaRad = Math.toRadians(g.getHeadTubeAngle());
            g.setTrail((radius * Math.cos(htaRad) - g.getForkOffset())     / Math.sin(htaRad));
        }
    }

    /**
     * Assigns each geometry row a size bucket (0 = smallest, 1 = next, …) within its bike model.
     * Sizes are sorted by numeric value where possible ("54cm" → 54, "47" → 47), falling back to
     * alpha ordering (XS < S < M < L < XL). This lets bikes from different brands be compared
     * within the same ordinal bucket (e.g. all "second-smallest" sizes together).
     *
     * @return flat map of "geometryKey::sizeLabel" → bucket integer
     */
    public Map<String, Integer> assignSizeBuckets(List<BikeGeometry> geometries) {
        // Collect distinct size labels per model, preserving first-seen order for deduplication
        Map<String, LinkedHashSet<String>> sizesByModel = new HashMap<>();
        for (BikeGeometry g : geometries) {
            // build map of geometry key -> list of available sizes
            sizesByModel.computeIfAbsent(g.getBikeGeometryKey(), k -> new LinkedHashSet<>())
                        .add(g.getSizeLabel());
        }

        Map<String, Integer> sizeBuckets = new HashMap<>();
        // for each geometry key, get its size list and sort smallest to largest
        // if sizeLabel is not a number, assign it to a number
        for (Map.Entry<String, LinkedHashSet<String>> entry : sizesByModel.entrySet()) {
            List<String> sortedSizes = entry.getValue().stream()
                    .sorted(Comparator.comparingDouble(GeometryMetricsService::parseSizeValue))
                    .toList();
            // for each sorted size for the current geometry key, create a new string value to represent the size, which
            // maps to a number 0 through the number of sizes, so that each size type falls to a scaled number
            // (i.e. smallest size = 0, largest size = sortedSizes.size() - 1)
            for (int i = 0; i < sortedSizes.size(); i++) {
                sizeBuckets.put(generateSizeBucketKey(entry.getKey(), sortedSizes.get(i)), i);
            }
        }
        return sizeBuckets;
    }

    /**
     * Computes min/max/mean/stdDev for each metric, bucketed by size ordinal rather than across
     * all sizes. Stats for ordinal 0 only include the smallest size of every model, ordinal 1 the
     * second-smallest, etc., so z-scores compare like-sized bikes.
     */
    public Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> computeStatsBySizeBucket(
            List<BikeGeometry> geometries, Map<String, Integer> sizeBuckets) {

        // maps {sizeBucket->{geometry metric->geometry values}}
        // so for size (0-n) for each geometry metric (trail, HTA, stack, etc.), store all values in order to calculate stats
        Map<Integer, Map<GeometryMetricKey, List<Double>>> valuesBySizeBucket = new HashMap<>();

        // build up geometry metrics for each size by looping through all geometry values
        for (BikeGeometry g : geometries) {
            // get the current geometry's size bucket
            Integer sizeBucket = sizeBuckets.get(generateSizeBucketKey(g.getBikeGeometryKey(), g.getSizeLabel()));
            if (sizeBucket == null) continue;

            Map<GeometryMetricKey, List<Double>> values =
                    valuesBySizeBucket.computeIfAbsent(sizeBucket, k -> new EnumMap<>(GeometryMetricKey.class));
            addGeometryValue(values, GeometryMetricKey.TRAIL, g.getTrail());
            addGeometryValue(values, GeometryMetricKey.WHEELBASE, g.getWheelbase());
            addGeometryValue(values, GeometryMetricKey.HEAD_ANGLE, g.getHeadTubeAngle());
            addGeometryValue(values, GeometryMetricKey.REACH, g.getReach());
            addGeometryValue(values, GeometryMetricKey.STACK, g.getStack());
            addGeometryValue(values, GeometryMetricKey.CHAINSTAY, g.getChainstay());
            addGeometryValue(values, GeometryMetricKey.SEAT_TUBE_LENGTH, g.getSeatTubeLength());
        }

        Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> result = new HashMap<>();
        // for each size bucket, calculate the stats for each geometry metric type and store in result to be returned
        for (Map.Entry<Integer, Map<GeometryMetricKey, List<Double>>> entry : valuesBySizeBucket.entrySet()) {
            Map<GeometryMetricKey, GeometryMetricsStats> statsForSizeBucket = new EnumMap<>(GeometryMetricKey.class);
            for (Map.Entry<GeometryMetricKey, List<Double>> metricEntry : entry.getValue().entrySet()) {
                statsForSizeBucket.put(metricEntry.getKey(), computeMetricsStats(metricEntry.getValue()));
            }
            result.put(entry.getKey(), statsForSizeBucket);
        }
        return result;
    }

    private static String generateSizeBucketKey(String geometryKey, String sizeLabel) {
        return geometryKey + "::" + sizeLabel;
    }

    /**
     * Converts a size label to a sortable double so sizes can be ordered smallest → largest.
     * Strips non-numeric suffixes for numeric sizes ("54cm" → 54.0), or maps alpha sizes to
     * a fixed scale (XS=0, S=1, M=2, L=3, XL=4, XXL=5).
     */
    private static double parseSizeValue(String sizeLabel) {
        if (sizeLabel == null) return Double.MAX_VALUE;
        String s = sizeLabel.trim().toLowerCase();

        String numericPart = s.replaceAll("[^\\d.]", "");
        if (!numericPart.isEmpty()) {
            try { return Double.parseDouble(numericPart); } catch (NumberFormatException ignored) {}
        }

        return switch (s) {
            case "xxs" -> -1;
            case "xs"  ->  0;
            case "s"   ->  1;
            case "m"   ->  2;
            case "l"   ->  3;
            case "xl"  ->  4;
            case "xxl" ->  5;
            default    -> Double.MAX_VALUE;
        };
    }

    private void addGeometryValue(Map<GeometryMetricKey, List<Double>> map, GeometryMetricKey key, Double value) {
        if (value != null) {
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private GeometryMetricsStats computeMetricsStats(List<Double> values) {
        double min = values.stream().min(Double::compare).orElse(0.0);
        double max = values.stream().max(Double::compare).orElse(0.0);
        double mean = values.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        return new GeometryMetricsStats(min, max, mean, stdDev);
    }

    // Given a BikeGeometry object, compute its RideCharacter using size-bucketed z-scores
    public RideCharacter getZGeometryMetrics(
            Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> statsByOrdinal,
            Map<String, Integer> sizeOrdinals,
            BikeGeometry bikeGeometry) {

        Integer ordinal = sizeOrdinals.get(generateSizeBucketKey(bikeGeometry.getBikeGeometryKey(), bikeGeometry.getSizeLabel()));
        Map<GeometryMetricKey, GeometryMetricsStats> stats =
                ordinal != null ? statsByOrdinal.getOrDefault(ordinal, Map.of()) : Map.of();

        double trail         = bikeGeometry.getTrail()         != null ? bikeGeometry.getTrail()         : 0.0;
        double wheelbase     = bikeGeometry.getWheelbase()     != null ? bikeGeometry.getWheelbase()     : 0.0;
        double chainstay     = bikeGeometry.getChainstay()     != null ? bikeGeometry.getChainstay()     : 0.0;
        double headTubeAngle = bikeGeometry.getHeadTubeAngle() != null ? bikeGeometry.getHeadTubeAngle() : 0.0;
        double reach         = bikeGeometry.getReach()         != null ? bikeGeometry.getReach()         : 0.0;
        double stack         = bikeGeometry.getStack()         != null ? bikeGeometry.getStack()         : 0.0;
        double seatTubeLength = bikeGeometry.getSeatTubeLength() != null ? bikeGeometry.getSeatTubeLength() : 0.0;

        double zTrail = calculateZ(stats, GeometryMetricKey.TRAIL, trail);
        double zWheelbase = calculateZ(stats, GeometryMetricKey.WHEELBASE, wheelbase);
        double zChainstay = calculateZ(stats, GeometryMetricKey.CHAINSTAY, chainstay);
        double zHeadTubeAngle = calculateZ(stats, GeometryMetricKey.HEAD_ANGLE, headTubeAngle);
        double zReach = calculateZ(stats, GeometryMetricKey.REACH, reach);
        double zStack = calculateZ(stats, GeometryMetricKey.STACK, stack);
        double zSeatTubeLength = calculateZ(stats, GeometryMetricKey.SEAT_TUBE_LENGTH, seatTubeLength);

        RideCharacter rc = new RideCharacter(bikeGeometry.getBikeGeometryKey(), bikeGeometry.getSizeLabel());

        // TODO: will tweak these weights eventually
        // Stability: higher = more stable
        rc.setStabilityZIndex(0.35 * zTrail + 0.30 * zWheelbase + 0.10 * zChainstay + 0.20 * (1 - zHeadTubeAngle));

        // Agility / Responsiveness
        rc.setAgilityZIndex(0.40 * zHeadTubeAngle + 0.25 * (1 - zTrail) + 0.20 * (1 - zWheelbase) + 0.15 * (1 - zChainstay));

        // Comfort
        rc.setComfortZIndex(0.50 * zStack + 0.30 * (1 - zReach) + 0.20 * zSeatTubeLength);

        // Aggression / Race
        rc.setAggressionZIndex(0.35 * zReach + 0.30 * zHeadTubeAngle + 0.20 * (1 - zStack) + 0.15 * (1 - zTrail));

        // Handling / Cornering
        rc.setHandlingZIndex(0.35 * zTrail + 0.30 * (1 - zWheelbase) + 0.15 * (1 - zChainstay) + 0.20 * zHeadTubeAngle);

        return rc;
    }

    private double calculateZ(Map<GeometryMetricKey, GeometryMetricsStats> stats, GeometryMetricKey key, double metricValue) {
        GeometryMetricsStats s = stats.get(key);
        if (s == null || s.stdDev() == 0) return 0.0;
        return (metricValue - s.mean()) / s.stdDev();
    }

    public void normalizeIndexes(List<RideCharacter> rideCharacters) {
        // Step 1: find min/max for each index
        double minStability = rideCharacters.stream().mapToDouble(RideCharacter::getStabilityZIndex).min().orElse(0);
        double maxStability = rideCharacters.stream().mapToDouble(RideCharacter::getStabilityZIndex).max().orElse(1);

        double minAgility = rideCharacters.stream().mapToDouble(RideCharacter::getAgilityZIndex).min().orElse(0);
        double maxAgility = rideCharacters.stream().mapToDouble(RideCharacter::getAgilityZIndex).max().orElse(1);

        double minComfort = rideCharacters.stream().mapToDouble(RideCharacter::getComfortZIndex).min().orElse(0);
        double maxComfort = rideCharacters.stream().mapToDouble(RideCharacter::getComfortZIndex).max().orElse(1);

        double minAggression = rideCharacters.stream().mapToDouble(RideCharacter::getAggressionZIndex).min().orElse(0);
        double maxAggression = rideCharacters.stream().mapToDouble(RideCharacter::getAggressionZIndex).max().orElse(1);

        double minHandling = rideCharacters.stream().mapToDouble(RideCharacter::getHandlingZIndex).min().orElse(0);
        double maxHandling = rideCharacters.stream().mapToDouble(RideCharacter::getHandlingZIndex).max().orElse(1);

        for (RideCharacter rc : rideCharacters) {
            rc.setStabilityIndex(normalize(rc.getStabilityZIndex(), minStability, maxStability));
            rc.setAgilityIndex(normalize(rc.getAgilityZIndex(), minAgility, maxAgility));
            rc.setComfortIndex(normalize(rc.getComfortZIndex(), minComfort, maxComfort));
            rc.setAggressionIndex(normalize(rc.getAggressionZIndex(), minAggression, maxAggression));
            rc.setHandlingIndex(normalize(rc.getHandlingZIndex(), minHandling, maxHandling));
        }
    }

    private double normalize(double value, double min, double max) {
        if (max == min) return 50; // edge case, all values identical
        return (value - min) / (max - min) * 100;
    }
}
