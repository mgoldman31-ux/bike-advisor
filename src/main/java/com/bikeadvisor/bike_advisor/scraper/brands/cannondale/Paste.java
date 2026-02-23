//import com.bikeadvisor.bike_advisor.model.BikeGeometry;
//import com.bikeadvisor.bike_advisor.model.BikeSummary;
//
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public static List<BikeGeometry> parseBikeGeometry(String ocrText, BikeSummary bikeSummary) {
//    List<BikeGeometry> geometries = new ArrayList<>();
//
//    String[] sizeLabels = extractSizeLabels(ocrText);
//    List<List<Double>> geometryRows = extractGeometryRows(ocrText);  // ← Much clearer!
//
//    for (int i = 0; i < sizeLabels.length && i < geometryRows.size(); i++) {
//        BikeGeometry geo = new BikeGeometry();
//        geo.setBikeSummary(bikeSummary);
//        geo.setSizeLabel(sizeLabels[i]);
//
//        List<Double> row = geometryRows.get(i);  // ← Super readable!
//
//        geo.setSeatTubeLength(getValue(row, 0));     // A
//        geo.setTopTubeEffective(getValue(row, 1));   // B
//        geo.setHeadTubeAngle(getValue(row, 3));      // D
//        geo.setSeatTubeAngleEffective(getValue(row, 4)); // E
//        geo.setStack(getValue(row, 14));             // O
//        geo.setReach(getValue(row, 15));             // P
//
//        geometries.add(geo);
//    }
//
//    return geometries;
//}
//
//private static List<List<Double>> extractGeometryRows(String cleanText) {
//    List<List<Double>> rows = new ArrayList<>();  // ← Clean!
//    String[] lines = cleanText.split("\n");
//
//    for (String line : lines) {
//        if (line.trim().isEmpty()) continue;
//
//        List<Double> row = new ArrayList<>();  // ← Natural!
//        String[] tokens = line.split("\\s+");
//
//        for (String token : tokens) {
//            try {
//                String cleanToken = token.replace("°", "");
//                row.add(Double.parseDouble(cleanToken));
//            } catch (NumberFormatException ignored) {
//                // Skip non-numbers
//            }
//        }
//
//        // Row must have 6+ values to be valid geometry
//        if (row.size() >= 6) {
//            rows.add(row);
//        }
//    }
//
//    return rows;
//}
//
//private static double getValue(List<Double> row, int index) {
//    return index < row.size() && row.get(index) != null ? row.get(index) : 0.0;
//}
