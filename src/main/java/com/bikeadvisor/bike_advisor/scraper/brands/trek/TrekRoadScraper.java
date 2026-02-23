package com.bikeadvisor.bike_advisor.scraper.brands.trek;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.bikeadvisor.bike_advisor.scraper.BrandScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrekRoadScraper implements BrandScraper {

    private static final Logger log = LoggerFactory.getLogger(TrekRoadScraper.class);
    private static final String ROAD_BIKES_URL =
            "https://www.trekbikes.com/us/en_US/bikes/road-bikes/gravel-bikes/all-road-bikes/c/B564/";
    private static final String SIZING_API_URL =
            "https://api.trekbikes.com/occ/v2/us/products/%s/sizing?lang=en_US&curr=USD";
    private static final Pattern PRODUCT_CODE_PATTERN = Pattern.compile("/p/(\\d+)/");

    @Override
    public String getBrandName() {
        return Brand.TREK.getBrandName();
    }

    @Override
    public List<BikeSummary> fetchModels() throws Exception {
        log.info("Fetching Trek models from {}", ROAD_BIKES_URL);
        Document doc = Jsoup.connect(ROAD_BIKES_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
        List<BikeSummary> models = TrekParser.parseBikeSummaryDoc(doc);
        log.info("Fetched {} Trek models", models.size());
        return models;
    }

    @Override
    public List<BikeGeometry> fetchGeometry(BikeSummary model) throws Exception {
        String code = extractProductCode(model.getProductUrl());
        String apiUrl = String.format(SIZING_API_URL, code);
        log.debug("Fetching geometry for '{}' from API: {}", model.getModel(), apiUrl);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        String json;
        try (InputStream in = conn.getInputStream()) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<BikeGeometry> geometries = TrekParser.parseGeometryJson(json, model.getGeometryKey());
        if (geometries.isEmpty()) {
            log.warn("No geometry returned from API for '{}' (code={})", model.getModel(), code);
        } else {
            log.debug("Parsed {} size(s) for '{}'", geometries.size(), model.getModel());
        }
        return geometries;
    }

    private static String extractProductCode(String productUrl) {
        Matcher m = PRODUCT_CODE_PATTERN.matcher(productUrl);
        if (m.find()) return m.group(1);
        throw new IllegalArgumentException("Cannot extract product code from URL: " + productUrl);
    }
}
