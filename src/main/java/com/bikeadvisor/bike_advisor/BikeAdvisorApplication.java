package com.bikeadvisor.bike_advisor;

import com.bikeadvisor.bike_advisor.export.CsvExporter;
import com.bikeadvisor.bike_advisor.export.CsvReader;
import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.GeometryMetricKey;
import com.bikeadvisor.bike_advisor.model.GeometryMetricsStats;
import com.bikeadvisor.bike_advisor.model.RideCharacter;
import com.bikeadvisor.bike_advisor.service.GeometryMetricsService;
import com.bikeadvisor.bike_advisor.service.ScrapingService;
import com.bikeadvisor.bike_advisor.service.ScrapeResult;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class BikeAdvisorApplication {

	public static void main(String[] args) throws Exception {
//		SpringApplication.run(BikeAdvisorApplication.class, args);

        // Already scraped, commenting out
//		ScrapingService scrapingService = new ScrapingService();
//		ScrapeResult result = scrapingService.scrapeAll();
//
//		CsvExporter exporter = new CsvExporter();
//		exporter.writeBikeSummaries(result.bikes(), Path.of("bikes.csv"));
//		exporter.writeGeometries(
//				result.geometriesByKey().values().stream().flatMap(Collection::stream).toList(),
//				Path.of("geometries.csv")
//		);

        // Read geometry data and compute stats for each metric
        CsvReader csvReader = new CsvReader();
        List<BikeGeometry> geometries = csvReader.readGeometries(Path.of("geometries.csv"));

        GeometryMetricsService metricsService = new GeometryMetricsService();
        metricsService.deriveTrailWhereMissing(geometries);

        Map<String, Integer> sizeOrdinals = metricsService.assignSizeOrdinals(geometries);
        Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> statsByOrdinal =
                metricsService.computeStatsBySizeOrdinal(geometries, sizeOrdinals);

        printStats(statsByOrdinal);

        List<RideCharacter> rideCharacters = new ArrayList<>();
        for (BikeGeometry bikeGeometry : geometries) {
            rideCharacters.add(metricsService.getZGeometryMetrics(statsByOrdinal, sizeOrdinals, bikeGeometry));
        }

        metricsService.normalizeIndexes(rideCharacters);
        System.out.println("Ride characters: " + rideCharacters.getFirst());

        CsvExporter csvExporter = new CsvExporter();
        csvExporter.writeRideCharacters(rideCharacters, Path.of("ride_characters.csv"));
	}

    private static void printStats(Map<Integer, Map<GeometryMetricKey, GeometryMetricsStats>> statsByOrdinal) {
        statsByOrdinal.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(ordinalEntry -> {
                    System.out.println("--- Size ordinal " + ordinalEntry.getKey() + " ---");
                    ordinalEntry.getValue().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(e -> {
                                GeometryMetricsStats s = e.getValue();
                                System.out.printf("  %-24s  min=%7.2f  max=%7.2f  mean=%7.2f  stdDev=%6.2f%n",
                                        e.getKey(), s.min(), s.max(), s.mean(), s.stdDev());
                            });
                });
    }
}
