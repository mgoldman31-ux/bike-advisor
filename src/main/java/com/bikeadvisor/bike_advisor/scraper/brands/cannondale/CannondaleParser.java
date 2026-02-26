package com.bikeadvisor.bike_advisor.scraper.brands.cannondale;

import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CannondaleParser {

    private static final Logger log = LoggerFactory.getLogger(CannondaleParser.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<BikeSummary> parse(String responseBody) throws Exception {

        List<BikeSummary> results = new ArrayList<>();

        JsonNode root = mapper.readTree(responseBody);
        String queryResultStr = root.path("Data").path("QueryResult").asText();

        if (queryResultStr.isEmpty()) {
            log.warn("QueryResult is empty — API response may have changed structure");
            return results;
        }

        JsonNode queryResult = mapper.readTree(queryResultStr);
        JsonNode resultArray = queryResult.path("results");

        if (!resultArray.isArray()) {
            log.warn("Expected 'results' array in QueryResult but got: {}", resultArray.getNodeType());
            return results;
        }

        for (JsonNode item : resultArray) {
            String model = item.path("title").asText();
            String url = item.path("uri").asText();
            double price = extractPrice(item);
            String geometryKey = item.path("raw").path("platform").asText();

            if (model.isEmpty() || url.isEmpty()) {
                log.debug("Skipping item with missing title or URL: {}", item);
                continue;
            }

            BikeSummary bike = new BikeSummary();
            bike.setModel(model);
            bike.setPrice(price > 0 ? price : null);
            bike.setProductUrl(url);
            bike.setBrand(Brand.CANNONDALE.getBrandName());
            bike.setGeometryKey(geometryKey);

            results.add(bike);
        }

        log.debug("Parsed {} Cannondale models from API response", results.size());
        return results;
    }

    private static double extractPrice(JsonNode item) {
        JsonNode raw = item.path("raw");
        double finalPrice = raw.path("finalprice").asDouble(0.0);
        double msrp = raw.path("msrp").asDouble(0.0);

        if (finalPrice > 0) {
            return finalPrice;
        }
        if (msrp > 0) {
            return msrp;
        }

        return 0.0;
    }
}
