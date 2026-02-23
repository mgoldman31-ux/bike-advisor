package com.bikeadvisor.bike_advisor;

import com.bikeadvisor.bike_advisor.export.CsvExporter;
import com.bikeadvisor.bike_advisor.service.ScrapingService;
import com.bikeadvisor.bike_advisor.service.ScrapeResult;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.Collection;

@SpringBootApplication
public class BikeAdvisorApplication {

	public static void main(String[] args) throws Exception {
//		SpringApplication.run(BikeAdvisorApplication.class, args);

		ScrapingService scrapingService = new ScrapingService();
		ScrapeResult result = scrapingService.scrapeAll();

		CsvExporter exporter = new CsvExporter();
		exporter.writeBikeSummaries(result.bikes(), Path.of("bikes.csv"));
		exporter.writeGeometries(
				result.geometriesByKey().values().stream().flatMap(Collection::stream).toList(),
				Path.of("geometries.csv")
		);
	}
}
