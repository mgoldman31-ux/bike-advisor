package com.bikeadvisor.bike_advisor.scraper.brands.giant;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.scraper.BrandScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GiantRoadScraper implements BrandScraper {

    private static final Logger log = LoggerFactory.getLogger(GiantRoadScraper.class);
    private static final String ROAD_BIKES_URL = "https://www.giant-bicycles.com/us/bikes/road-bikes";

    @Override
    public String getBrandName() {
        return "Giant";
    }

    @Override
    public List<BikeSummary> fetchModels() throws Exception {
        log.info("Fetching Giant models from {}", ROAD_BIKES_URL);
        Document doc = Jsoup.connect(ROAD_BIKES_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
        List<BikeSummary> models = GiantParser.parseBikeSummaryDoc(doc, this::fetchPage);
        log.info("Fetched {} Giant models", models.size());
        return models;
    }

    private Document fetchPage(String url) {
        try {
            log.debug("Fetching page: {}", url);
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
        } catch (Exception e) {
            log.error("Failed to fetch page: {} — {}", url, e.getMessage());
            return null;
        }
    }

    @Override
    public List<BikeGeometry> fetchGeometry(BikeSummary model) throws Exception {
        log.debug("Fetching geometry for '{}' from {}", model.getModel(), model.getProductUrl());
        Document doc = fetchPage(model.getProductUrl());
        if (doc == null) throw new Exception("Failed to fetch product page: " + model.getProductUrl());
        List<BikeGeometry> geometries = GiantParser.parseGeometryTableDoc(doc, model.getGeometryKey());
        if (geometries.isEmpty()) {
            log.warn("No geometry found for '{}' (key={})", model.getModel(), model.getGeometryKey());
        } else {
            log.debug("Parsed {} size(s) for '{}'", geometries.size(), model.getModel());
        }
        return geometries;
    }
}
