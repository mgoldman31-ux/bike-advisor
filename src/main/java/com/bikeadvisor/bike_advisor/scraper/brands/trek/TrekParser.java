package com.bikeadvisor.bike_advisor.scraper.brands.trek;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.bikeadvisor.bike_advisor.model.Discipline;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrekParser {

    private static final Logger log = LoggerFactory.getLogger(TrekParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // Matches "id":"<digits>" followed (within the same object) by "name":"<value>"
    private static final Pattern IMPRESSION_PATTERN =
            Pattern.compile("\"id\"\\s*:\\s*\"(\\d+)\"[^}]*?\"name\"\\s*:\\s*\"([^\"]+)\"");

    private static final String BASE_URL = "https://www.trekbikes.com";

    /**
     * Parses the Trek road bikes product list.
     *
     * Trek's listing page renders bikes as <product-card-item :product="{...}"> Vue components.
     * The `:product` JSON attribute contains the URL and price but name is null, so model names
     * are extracted from the dataLayer analytics script embedded on the same page, keyed by
     * product code.
     */
    public static List<BikeSummary> parseBikeSummaryDoc(Document doc) {
        // Build code → name map from the embedded dataLayer impressions array
        Map<String, String> codeToName = parseDataLayerNames(doc);
        log.debug("Extracted {} model names from dataLayer", codeToName.size());

        List<BikeSummary> results = new ArrayList<>();

        Elements items = doc.select("product-card-item");
        log.debug("Found {} product-card-item elements", items.size());

        for (Element el : items) {
            String productJson = el.attr(":product");
            if (productJson.isBlank()) {
                log.debug("Skipping product-card-item with empty :product attribute");
                continue;
            }

            try {
                JsonNode product = objectMapper.readTree(productJson);
                String code = product.path("code").asText("");
                String url  = product.path("url").asText("");
                if (url.isEmpty()) continue;

                String name = codeToName.get(code);
                if (name == null || name.isEmpty()) {
                    log.warn("No name found in dataLayer for product code={}, url={}", code, url);
                    continue;
                }
                if (name.contains("Frameset")) continue;

                String priceText = product.path("price").path("formattedValue").asText(null);
                String normalizedUrl = url.replaceAll("\\?.*$", "");
                String productUrl = BASE_URL + normalizedUrl;
                String geometryKey = normalizedUrl.replaceAll("/p/.*$", "").replaceAll("^.*/", "");

                BikeSummary bike = new BikeSummary();
                bike.setBrand(Brand.TREK.getBrandName());
                bike.setModel(name);
                bike.setProductUrl(productUrl);
                bike.setPriceText(priceText);
                bike.setGeometryKey(geometryKey);
                bike.setDiscipline(Discipline.ROAD);
                results.add(bike);

            } catch (Exception e) {
                log.warn("Failed to parse :product JSON for element: {}", e.getMessage());
            }
        }

        return results;
    }

    /**
     * Extracts a code→name map from the dataLayer analytics script embedded in the page.
     * Trek includes an impressions array with accurate model names keyed by product id/code.
     */
    private static Map<String, String> parseDataLayerNames(Document doc) {
        Map<String, String> map = new HashMap<>();
        for (Element script : doc.select("script2")) {
            String content = script.html();
            if (!content.contains("impressions")) continue;
            Matcher m = IMPRESSION_PATTERN.matcher(content);
            while (m.find()) {
                map.put(m.group(1), m.group(2));
            }
        }
        return map;
    }

    /**
     * Parses geometry from the Trek sizing API response.
     * Endpoint: https://api.trekbikes.com/occ/v2/us/products/{code}/sizing?lang=en_US&curr=USD
     *
     * The response contains a geometryDataHeaders array (column names) and a geometryData array
     * where each entry holds a geometry array of values aligned to those headers.
     * Values are in cm; lengths are multiplied by 10 to convert to mm.
     */
    public static List<BikeGeometry> parseGeometryJson(String json, String geometryKey) throws Exception {
        JsonNode root    = objectMapper.readTree(json);
        JsonNode headers = root.path("geometryDataHeaders");
        JsonNode rows    = root.path("geometryData");

        if (!headers.isArray() || !rows.isArray()) {
            log.warn("Unexpected API response structure for key='{}'", geometryKey);
            return List.of();
        }

        List<String> headerList = new ArrayList<>();
        for (JsonNode h : headers) {
            headerList.add(h.asText());
        }

        List<BikeGeometry> geometries = new ArrayList<>();
        for (JsonNode row : rows) {
            JsonNode values = row.path("geometry");
            if (!values.isArray() || values.isEmpty()) continue;

            BikeGeometry geo = new BikeGeometry();
            geo.setBikeGeometryKey(geometryKey);

            for (int i = 0; i < Math.min(values.size(), headerList.size()); i++) {
                String header   = headerList.get(i);
                String cellText = values.get(i).asText();
                if (i == 0) {
                    geo.setSizeLabel(cellText);  // e.g. "47cm"
                } else {
                    applyGeometryValue(geo, header, cellText);
                }
            }

            geometries.add(geo);
        }

        return geometries;
    }

    private static void applyGeometryValue(BikeGeometry geo, String header, String cellText) {
        Double raw = parseDouble(cellText);
        if (raw == null) return;

        // Headers starting with "geometryAngle" are in degrees — no unit conversion.
        // All other measurement headers are in cm — multiply by 10 to convert to mm.
        boolean isAngle = header.startsWith("geometryAngle");
        double value = isAngle ? raw : raw * 10.0;

        switch (header) {
            case "geometrySeattube"        -> geo.setSeatTubeLength(value);
            case "geometryAngleSeattube"   -> geo.setSeatTubeAngleEffective(value);
            case "geometryLengthHeadtube"  -> geo.setHeadTubeLength(value);
            case "geometryAngleHead"       -> geo.setHeadTubeAngle(value);
            case "geometryEffToptube"      -> geo.setTopTubeEffective(value);
            case "geometryBBDrop"          -> geo.setBbDrop(value);
            case "geometryLengthChainstay" -> geo.setChainstay(value);
            case "geometryOffset"          -> geo.setForkOffset(value);
            case "geometryTrail"           -> geo.setTrail(value);
            case "geometryWheelbase"       -> geo.setWheelbase(value);
            case "geometryStandover"       -> geo.setStandover(value);
            case "geometryFrameReach"      -> geo.setReach(value);
            case "geometryFrameStack"      -> geo.setStack(value);
            // geometrySizeWheel, saddle rail heights — ignored
        }
    }

    private static Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim().replaceAll("[^\\d.,-]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
