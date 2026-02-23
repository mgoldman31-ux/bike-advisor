package com.bikeadvisor.bike_advisor.scraper.brands.cannondale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

public class CannondaleClient {

    private static final Logger log = LoggerFactory.getLogger(CannondaleClient.class);
    private static final String API_URL = "https://www.cannondale.com/api/coveo/search";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String fetchRoadRoadGravelJson() throws Exception {
        String roadPayload = """
        {
          "ConstantQueryExpression": "@source==Cannondale-Push",
          "AdvancedQueryExpression": "@language==\\"en-US\\" @firstavailableon<=today+0d @categories=('97080A93-BC36-45EE-B4FD-B7004E2C9A21','94210EA4-0750-40A9-AAC6-7E682CE039FD')",
          "Pipeline": "cannondale",
          "EnableDuplicateFilter": false,
          "NumberOfResults": 1000,
          "SearchHub": "PLP_/en-us/bikes/road_Listing"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (compatible; BikeGeoCollector/0.1)")
                .POST(HttpRequest.BodyPublishers.ofString(roadPayload))
                .build();

        Instant start = Instant.now();
        log.info("Sending Cannondale fetch models request");

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        Instant end = Instant.now();
        log.info("Cannondale fetch models response received in {}ms, status={}", end.toEpochMilli() - start.toEpochMilli(), response.statusCode());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Coveo search failed, status=" + response.statusCode()
                    + ", body=" + response.body());
        }

        return response.body();  // JSON string
    }
}
