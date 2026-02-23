package com.bikeadvisor.bike_advisor.service;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.scraper.BrandScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.cannondale.CannondaleRoadScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.giant.GiantRoadScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.specialized.SpecializedRoadScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.trek.TrekRoadScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ScrapingService {

    private static final Logger log = LoggerFactory.getLogger(ScrapingService.class);

    private final List<BrandScraper> scrapers = List.of(
            new CannondaleRoadScraper(),
            new GiantRoadScraper(),
            new SpecializedRoadScraper(),
            new TrekRoadScraper()
    );

    /**
     * Runs all brand scrapers in parallel and merges into a single ScrapeResult.
     * Each brand builds its own local collections — no shared mutable state between threads.
     */
    public ScrapeResult scrapeAll() {
        List<Future<ScrapeResult>> futures;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            futures = scrapers.stream()
                    .map(scraper -> executor.submit(() -> scrapeOneBrand(scraper)))
                    .toList();
        }

        // All tasks are done — merge brand results into a single ScrapeResult
        List<BikeSummary> allBikes = new ArrayList<>();
        Map<String, List<BikeGeometry>> allGeometries = new LinkedHashMap<>();

        for (Future<ScrapeResult> future : futures) {
            try {
                ScrapeResult brandResult = future.get();
                allBikes.addAll(brandResult.bikes());
                allGeometries.putAll(brandResult.geometriesByKey());
            } catch (Exception e) {
                log.error("Scraper task failed unexpectedly: {}", e.getMessage());
            }
        }

        log.info("Scrape complete — {} models, {} unique geometry entries across all brands",
                allBikes.size(), allGeometries.size());
        return new ScrapeResult(allBikes, allGeometries);
    }

    /**
     * Scrapes one brand fully and returns its results.
     * geometriesByKey holds one entry per unique frame — the same list is referenced by all
     * bikes that share that geometryKey.
     */
    private ScrapeResult scrapeOneBrand(BrandScraper scraper) {
        String brand = scraper.getBrandName();
        List<BikeSummary> bikes = new ArrayList<>();
        Map<String, List<BikeGeometry>> geometriesByKey = new LinkedHashMap<>();

        List<BikeSummary> models;
        try {
            models = scraper.fetchModels();
        } catch (Exception e) {
            log.error("Failed to fetch models for {}: {}", brand, e.getMessage());
            return new ScrapeResult(bikes, geometriesByKey);
        }
        log.info("Fetched {} {} models, fetching geometry...", models.size(), brand);

        for (BikeSummary model : models) {
            String key = model.getGeometryKey();
            if (!geometriesByKey.containsKey(key)) {
                try {
                    geometriesByKey.put(key, scraper.fetchGeometry(model));
                } catch (Exception e) {
                    log.error("Failed to fetch geometry for '{}' (key={}): {}", model.getModel(), key, e.getMessage());
                    geometriesByKey.put(key, new ArrayList<>());
                }
            }
            bikes.add(model);
        }
        log.info("Geometry fetch complete for {}", brand);

        return new ScrapeResult(bikes, geometriesByKey);
    }
}
