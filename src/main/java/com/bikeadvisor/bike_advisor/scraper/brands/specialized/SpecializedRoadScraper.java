package com.bikeadvisor.bike_advisor.scraper.brands.specialized;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.bikeadvisor.bike_advisor.scraper.BrandScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SpecializedRoadScraper implements BrandScraper {

    private static final Logger log = LoggerFactory.getLogger(SpecializedRoadScraper.class);
    private static final String ROAD_BIKES_URL = "https://www.specialized.com/us/en/shop/bikes/road-bikes";

    @Override
    public String getBrandName() {
        return Brand.SPECIALIZED.getBrandName();
    }

    @Override
    public List<BikeSummary> fetchModels() throws Exception {
        log.info("Fetching Specialized models from {}", ROAD_BIKES_URL);
        Document doc = Jsoup.connect(ROAD_BIKES_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
        List<BikeSummary> models = SpecializedParser.parseBikeSummaryDoc(doc);
        log.info("Fetched {} Specialized models", models.size());
        return models;
    }

    @Override
    public List<BikeGeometry> fetchGeometry(BikeSummary model) throws Exception {
        log.debug("Fetching geometry for '{}' from {}", model.getModel(), model.getProductUrl());
        Document doc = Jsoup.connect(model.getProductUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
        List<BikeGeometry> geometries = SpecializedParser.parseGeometryTableDoc(doc, model.getGeometryKey());
        if (geometries.isEmpty()) {
            log.warn("No geometry found for '{}' (key={})", model.getModel(), model.getGeometryKey());
        } else {
            log.debug("Parsed {} size(s) for '{}'", geometries.size(), model.getModel());
        }
        return geometries;
    }
}
