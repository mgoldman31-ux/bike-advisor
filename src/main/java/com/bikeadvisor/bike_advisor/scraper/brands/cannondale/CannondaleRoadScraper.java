package com.bikeadvisor.bike_advisor.scraper.brands.cannondale;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.bikeadvisor.bike_advisor.scraper.BrandScraper;
import com.bikeadvisor.bike_advisor.scraper.GeometryVisionExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CannondaleRoadScraper implements BrandScraper {

    private static final Logger log = LoggerFactory.getLogger(CannondaleRoadScraper.class);

    private final CannondaleClient httpClient = new CannondaleClient();
    private final GeometryVisionExtractor visionExtractor = new GeometryVisionExtractor();

    @Override
    public String getBrandName() {
        return Brand.CANNONDALE.getBrandName();
    }

    @Override
    public List<BikeSummary> fetchModels() throws Exception {
        log.info("Fetching Cannondale models via API");
        String httpResponse = httpClient.fetchRoadRoadGravelJson();
        List<BikeSummary> models = CannondaleParser.parse(httpResponse);
        log.info("Fetched {} Cannondale models", models.size());
        return models;
    }

    @Override
    public List<BikeGeometry> fetchGeometry(BikeSummary model) throws Exception {
        log.debug("Fetching geometry for '{}' from {}", model.getModel(), model.getProductUrl());
        String imageUrl = extractGeometryImage(model.getProductUrl());
        List<BikeGeometry> geometries = visionExtractor.extractGeometry(imageUrl, model.getGeometryKey());
        if (geometries.isEmpty()) {
            log.warn("No geometry sizes returned for '{}' (key={})", model.getModel(), model.getGeometryKey());
        } else {
            log.debug("Parsed {} size(s) for '{}'", geometries.size(), model.getModel());
        }
        return geometries;
    }

    private String extractGeometryImage(String productUrl) throws Exception {
        log.debug("Fetching product page for geometry image: {}", productUrl);
        Document doc = Jsoup.connect(productUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        var img = doc.selectFirst(".sizing-image picture img");

        if (img != null) {
            String src = img.attr("abs:src").replaceAll("\\?.*$", "");
            log.debug("Found geometry image via primary selector: {}", src);
            return src;
        }

        String[] fallbacks = {
                ".expandable-item__content[data-totoggle=product-sizing] img",
                ".sizing-image img",
                "img[alt=Details][src*='geotable']"
        };

        for (String fallback : fallbacks) {
            img = doc.selectFirst(fallback);
            if (img != null) {
                String src = img.attr("abs:src").replaceAll("\\?.*$", "");
                log.warn("Primary selector failed for {}; found geometry image via fallback '{}': {}",
                        productUrl, fallback, src);
                return src;
            }
        }

        throw new Exception("Geometry image not found for: " + productUrl);
    }
}
