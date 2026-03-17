package com.bikeadvisor.bike_advisor.model;

import java.util.Map;

/**
 * Output of a PCA fit: per-bike component scores, loadings, and variance explained.
 * Keys in pcScores use the format "geometryKey::sizeLabel".
 */
public record PcaResult(
        Map<String, double[]> pcScores,
        double[][] loadings,            // loadings[componentIndex][metricIndex]
        double[] varianceExplained,     // fraction of total variance per component
        int numComponents) {

    public static PcaResult empty() {
        return new PcaResult(Map.of(), new double[0][0], new double[0], 0);
    }

    public boolean isEmpty() {
        return numComponents == 0;
    }
}
