package com.bikeadvisor.bike_advisor.service;

import com.bikeadvisor.bike_advisor.export.CsvExporter;
import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.GeometryMetricKey;
import com.bikeadvisor.bike_advisor.model.GeometryMetricsStats;
import com.bikeadvisor.bike_advisor.model.RideCharacter;
import com.bikeadvisor.bike_advisor.repository.BikeGeometryRepository;
import com.bikeadvisor.bike_advisor.repository.BikeSummaryRepository;
import com.bikeadvisor.bike_advisor.repository.RideCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DataRefreshService {

    private static final Logger log = LoggerFactory.getLogger(DataRefreshService.class);

    private final ScrapingService scrapingService;
    private final GeometryMetricsService metricsService;
    private final CsvExporter csvExporter;
    private final BikeSummaryRepository bikeSummaryRepository;
    private final BikeGeometryRepository bikeGeometryRepository;
    private final RideCharacterRepository rideCharacterRepository;

    public DataRefreshService(
            ScrapingService scrapingService,
            GeometryMetricsService metricsService,
            CsvExporter csvExporter,
            BikeSummaryRepository bikeSummaryRepository,
            BikeGeometryRepository bikeGeometryRepository,
            RideCharacterRepository rideCharacterRepository) {
        this.scrapingService = scrapingService;
        this.metricsService = metricsService;
        this.csvExporter = csvExporter;
        this.bikeSummaryRepository = bikeSummaryRepository;
        this.bikeGeometryRepository = bikeGeometryRepository;
        this.rideCharacterRepository = rideCharacterRepository;
    }

    /**
     * Full pipeline: scrape all brands → derive missing trail values → compute size-bucketed
     * stats → build and normalize ride character indexes → write CSVs → persist to DB.
     */
    public void refresh() throws IOException {
        log.info("Starting data refresh...");

        // 1. Scrape bikes and geometries
        ScrapeResult result = scrapingService.scrapeAll();
        List<BikeGeometry> geometries = result.geometriesByKey().values().stream()
                .flatMap(List::stream)
                .toList();
        log.info("Scraped {} bikes, {} geometry rows", result.bikes().size(), geometries.size());

        // 2. Derive missing trail values from wheel size + head tube angle
        metricsService.deriveTrailWhereMissing(geometries);

        // 3. Compute size-bucketed stats (mean, min, max, stddev) for each bucket
        Map<String, Integer> sizeBuckets = metricsService.assignSizeBuckets(geometries);
        Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> statsByBucket =
                metricsService.computeStatsBySizeBucket(geometries, sizeBuckets);

        // 4. Build ride character indexes normalized to 0–100
        List<RideCharacter> rideCharacters = geometries.stream()
                .map(g -> metricsService.getZGeometryMetrics(statsByBucket, sizeBuckets, g))
                .toList();
        metricsService.normalizeIndexes(rideCharacters);
        log.info("Computed ride characters for {} geometry rows", rideCharacters.size());

        // 5. Write CSVs (eventually we'll skip this, just persist in DB)
        csvExporter.writeBikeSummaries(result.bikes(), Path.of("bikes.csv"));
        csvExporter.writeGeometries(geometries, Path.of("geometries.csv"));
        csvExporter.writeRideCharacters(rideCharacters, Path.of("ride-characters.csv"));

        // 6. Persist to DB
        bikeSummaryRepository.saveAll(result.bikes());
        bikeGeometryRepository.saveAll(geometries);
        rideCharacterRepository.saveAll(rideCharacters);

        log.info("Data refresh complete.");
    }
}
