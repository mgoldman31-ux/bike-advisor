package com.bikeadvisor.bike_advisor.service;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.GeometryMetricKey;
import com.bikeadvisor.bike_advisor.model.GeometryMetricsStats;
import com.bikeadvisor.bike_advisor.model.PcaResult;
import com.bikeadvisor.bike_advisor.model.RideCharacter;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bikeadvisor.bike_advisor.model.GeometryMetricKey.*;

@Service
public class PcaService {

    private static final Logger log = LoggerFactory.getLogger(PcaService.class);
    private static final int NUM_COMPONENTS = 3;

    // Geometry metrics used as PCA inputs, in a fixed order.
    // This order must match the columns of INDEX_SIGNATURES below.
    static final GeometryMetricKey[] METRICS = {
            TRAIL, WHEELBASE, HEAD_ANGLE, REACH, STACK, CHAINSTAY, BB_DROP
    };

    // Expected loading signatures for each named ride character index.
    // Column order matches METRICS: TRAIL, WHEELBASE, HEAD_ANGLE, REACH, STACK, CHAINSTAY, BB_DROP
    // +1 = this metric being high pushes the score up; -1 = inverse; 0 = not expected to load heavily
    private static final double[][] INDEX_SIGNATURES = {
            { +1,  +1,  -1,   0,   0,   0,   0 }, // STABILITY: long trail/wheelbase, relaxed HTA
            {  0,   0,  +1,  +1,  -1,   0,  -1 }, // AERO:      steep HTA, long reach, low stack, low BB drop
            { -1,   0,   0,   0,   0,   0,   0 }, // AGILITY:   short trail = more responsive handling
    };

    private static final String[] INDEX_NAMES = {
            "STABILITY", "AERO", "AGILITY"
    };

    /**
     * Fits PCA on the standardized geometry matrix.
     * Rows are bikes already z-scored within their size bucket (so size effects are removed).
     * Returns the PC scores for each bike and the loading matrix for interpretation.
     */
    public PcaResult fit(
            List<BikeGeometry> geometries,
            Map<String, Integer> sizeBuckets,
            Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> statsByBucket) {

        List<String> sizeKeys = new ArrayList<>();
        List<double[]> allZScores = new ArrayList<>();

        // for each geometry, calculate its z-scores for all geometry metrics
        for (BikeGeometry g : geometries) {
            String key = sizeKey(g);
            Integer bucket = sizeBuckets.get(key);
            if (bucket == null) continue;

            double[] zScoresVector = computeZScores(g, statsByBucket.getOrDefault(bucket, Map.of()));
            if (zScoresVector == null) continue;

            sizeKeys.add(key);
            allZScores.add(zScoresVector);
        }

        // Need more data points than geometry metrics to run PCA to calculate meaningful variation
        if (allZScores.size() < METRICS.length + 1) {
            log.warn("PCA: only {} valid rows (need > {}), returning empty result", allZScores.size(), METRICS.length);
            return PcaResult.empty();
        }

        log.info("PCA: fitting on {} geometry rows ({} skipped due to missing metrics)",
                allZScores.size(), geometries.size() - allZScores.size());

        RealMatrix data = MatrixUtils.createRealMatrix(allZScores.toArray(new double[0][]));
        SingularValueDecomposition svd = new SingularValueDecomposition(data);

        // tells us how much variance each component captures, the "stretch".
        // singularValues will have an entry for each PC
        double[] singularValues = svd.getSingularValues();

        // variance is sum of squared distance of all data points from origin, this is just the variance calculation
        double totalVariance = 0;
        for (double sv : singularValues) totalVariance += sv * sv;

        int numComponents = Math.min(NUM_COMPONENTS, singularValues.length);
        double[] varianceExplained = new double[numComponents];
        // PC loading for each component by metric
        double[][] loadings = new double[numComponents][METRICS.length];

        // V Matrix: direction of PCs across metrics
        RealMatrix vMatrix = svd.getV();
        for (int c = 0; c < numComponents; c++) {
            varianceExplained[c] = (singularValues[c] * singularValues[c]) / totalVariance;
            for (int m = 0; m < METRICS.length; m++) {
                loadings[c][m] = vMatrix.getEntry(m, c); // builds loading table
            }
        }

        // PC scores for each bike: row r of (U * diag(S))
        RealMatrix uMatrix = svd.getU(); // U matrix contains each bike's coordinates in the PCA space
        Map<String, double[]> pcScores = new HashMap<>();
        // for each bike, scale according to singular values (one for each PC)
        for (int r = 0; r < sizeKeys.size(); r++) {
            double[] scores = new double[numComponents];
            for (int c = 0; c < numComponents; c++) {
                scores[c] = uMatrix.getEntry(r, c) * singularValues[c]; // calculate bike r's score on component c
            }
            pcScores.put(sizeKeys.get(r), scores);
        }

        logLoadings(loadings, varianceExplained);
        return new PcaResult(pcScores, loadings, varianceExplained, numComponents);
    }

    /**
     * Maps each named ride character index to its PC and builds RideCharacter objects.
     * Raw PC scores are stored in the named index fields; normalizeIndexes() scales them 0–100.
     */
    public List<RideCharacter> buildRideCharacters(
            List<BikeGeometry> geometries, PcaResult pca, Map<String, Integer> sizeBuckets) {
        List<RideCharacter> result = new ArrayList<>();

        if (pca.isEmpty()) {
            log.warn("PCA result is empty — ride characters will have zero scores");
            for (BikeGeometry g : geometries) {
                result.add(new RideCharacter(g.getBikeGeometryKey(), g.getSizeLabel()));
            }
            return result;
        }

        int[] mapping = mapIndexesToComponents(pca.loadings());
        double[] signs = new double[INDEX_SIGNATURES.length];
        for (int i = 0; i < INDEX_SIGNATURES.length; i++) {
            signs[i] = getSign(pca.loadings(), mapping[i], INDEX_SIGNATURES[i]);
        }
        logMapping(mapping, signs);

        for (BikeGeometry g : geometries) {
            RideCharacter rc = new RideCharacter(g.getBikeGeometryKey(), g.getSizeLabel());
            String currSizeKey = sizeKey(g);
            rc.setSizeBucket(sizeBuckets.get(currSizeKey));
            double[] scores = pca.pcScores().get(currSizeKey);
            if (scores != null) {
                rc.setStabilityIndex(signs[0] * scores[mapping[0]]);
                rc.setAeroIndex(     signs[1] * scores[mapping[1]]);
                rc.setAgilityIndex(  signs[2] * scores[mapping[2]]);
            }
            result.add(rc);
        }

        return result;
    }

    /**
     * Greedy assignment: for each named index (in order), picks the unused PC whose loading
     * vector has the highest absolute dot product with the index's expected signature.
     */
    int[] mapIndexesToComponents(double[][] loadings) {
        int[] mapping = new int[INDEX_SIGNATURES.length];
        boolean[] used = new boolean[loadings.length];

        for (int i = 0; i < INDEX_SIGNATURES.length; i++) {
            double best = Double.NEGATIVE_INFINITY;
            int bestPc = 0;
            for (int pc = 0; pc < loadings.length; pc++) {
                if (used[pc]) continue;
                double score = Math.abs(dotProduct(loadings[pc], INDEX_SIGNATURES[i]));
                if (score > best) {
                    best = score;
                    bestPc = pc;
                }
            }
            mapping[i] = bestPc;
            used[bestPc] = true;
        }

        return mapping;
    }

    double getSign(double[][] loadings, int pc, double[] signature) {
        return dotProduct(loadings[pc], signature) >= 0 ? 1.0 : -1.0;
    }

    /**
     * Returns a human-readable summary of the PCA result: loading matrix, variance explained,
     * and which PC was mapped to each named index.
     */
    public String formatSummary(PcaResult pca) {
        if (pca.isEmpty()) return "PCA result is empty.";

        int[] mapping = mapIndexesToComponents(pca.loadings());
        double[] signs = new double[INDEX_SIGNATURES.length];
        for (int i = 0; i < INDEX_SIGNATURES.length; i++) {
            signs[i] = getSign(pca.loadings(), mapping[i], INDEX_SIGNATURES[i]);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-16s", "Metric"));
        for (int c = 0; c < pca.numComponents(); c++) sb.append(String.format("  PC%-7d", c + 1));
        sb.append("\n");

        for (int m = 0; m < METRICS.length; m++) {
            sb.append(String.format("%-16s", METRICS[m].name()));
            for (int c = 0; c < pca.numComponents(); c++) {
                sb.append(String.format("  %+.4f  ", pca.loadings()[c][m]));
            }
            sb.append("\n");
        }

        sb.append(String.format("%-16s", "Var explained"));
        for (double v : pca.varianceExplained()) sb.append(String.format("  %.1f%%    ", v * 100));
        sb.append("\n\nIndex mapping:\n");

        for (int i = 0; i < INDEX_NAMES.length; i++) {
            sb.append(String.format("  %-12s → PC%d (%s)\n",
                    INDEX_NAMES[i], mapping[i] + 1, signs[i] > 0 ? "+" : "-"));
        }

        return sb.toString();
    }

    // Given a geometry and stats for each geometry metric, calculate z-score vector
    private double[] computeZScores(BikeGeometry g, Map<GeometryMetricKey, GeometryMetricsStats> stats) {
        double[] geometryMetricZScores = new double[METRICS.length];
        for (int i = 0; i < METRICS.length; i++) {
            Double rawGeometryValue = getRawGeometryValue(g, METRICS[i]);
            GeometryMetricsStats s = stats.get(METRICS[i]);
            if (rawGeometryValue == null || s == null || s.stdDev() == 0) return null;
            geometryMetricZScores[i] = (rawGeometryValue - s.mean()) / s.stdDev();
        }
        return geometryMetricZScores;
    }

    private Double getRawGeometryValue(BikeGeometry g, GeometryMetricKey key) {
        return switch (key) {
            case TRAIL      -> g.getTrail();
            case WHEELBASE  -> g.getWheelbase();
            case HEAD_ANGLE -> g.getHeadTubeAngle();
            case REACH      -> g.getReach();
            case STACK      -> g.getStack();
            case CHAINSTAY  -> g.getChainstay();
            case BB_DROP    -> g.getBbDrop();
            default         -> null;
        };
    }

    private static String sizeKey(BikeGeometry g) {
        return g.getBikeGeometryKey() + "::" + g.getSizeLabel();
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) sum += a[i] * b[i];
        return sum;
    }

    private void logLoadings(double[][] loadings, double[] varianceExplained) {
        StringBuilder sb = new StringBuilder("\nPCA Loadings:\n");
        sb.append(String.format("%-16s", "Metric"));
        for (int c = 0; c < loadings.length; c++) sb.append(String.format("  PC%-7d", c + 1));
        sb.append("\n");
        for (int m = 0; m < METRICS.length; m++) {
            sb.append(String.format("%-16s", METRICS[m].name()));
            for (int c = 0; c < loadings.length; c++) sb.append(String.format("  %+.4f  ", loadings[c][m]));
            sb.append("\n");
        }
        sb.append(String.format("%-16s", "Var explained"));
        for (double v : varianceExplained) sb.append(String.format("  %.1f%%    ", v * 100));
        log.info("{}", sb);
    }

    private void logMapping(int[] mapping, double[] signs) {
        StringBuilder sb = new StringBuilder("PC → index mapping:  ");
        for (int i = 0; i < INDEX_NAMES.length; i++) {
            sb.append(String.format("%s=PC%d(%s)  ", INDEX_NAMES[i], mapping[i] + 1, signs[i] > 0 ? "+" : "-"));
        }
        log.info("{}", sb);
    }
}
