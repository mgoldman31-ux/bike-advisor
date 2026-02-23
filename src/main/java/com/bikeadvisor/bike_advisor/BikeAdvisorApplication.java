package com.bikeadvisor.bike_advisor;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Brand;
import com.bikeadvisor.bike_advisor.scraper.brands.cannondale.CannondaleRoadScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.giant.GiantRoadScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.specialized.SpecializedRoadScraper;
import com.bikeadvisor.bike_advisor.scraper.brands.trek.TrekRoadScraper;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class BikeAdvisorApplication {

	public static void main(String[] args) throws Exception {
//		SpringApplication.run(BikeAdvisorApplication.class, args);

        // for now, let's just put all BikeSummary's mapped to a geometry list.
        Map<BikeSummary, List<BikeGeometry>> allBikes = new HashMap<>();

        Map<String, List<BikeGeometry>> cannondaleGeometryByKey = new HashMap<>();
        final CannondaleRoadScraper cannondaleScraper = new CannondaleRoadScraper();
        final List<BikeSummary> cannondaleRoadBikes = cannondaleScraper.fetchModels();
        for (BikeSummary cannondaleBike : cannondaleRoadBikes) {
            List<BikeGeometry> geometries;
            if (cannondaleGeometryByKey.containsKey(cannondaleBike.getGeometryKey())) {
                geometries = cannondaleGeometryByKey.get(cannondaleBike.getGeometryKey());
            } else {
                try {
                    geometries = cannondaleScraper.fetchGeometry(cannondaleBike);
                    cannondaleGeometryByKey.put(cannondaleBike.getGeometryKey(), geometries);
                } catch (Exception e) {
                    geometries =  new ArrayList<>();
                    System.out.println("[Main] Error: " + e.getMessage());
                }
            }
            allBikes.put(cannondaleBike, geometries);
        }
        int cannondaleBikes = allBikes.size();
        System.out.println("Found " + allBikes.size() + " Cannondale bikes.");

        GiantRoadScraper giantRoadScraper = new GiantRoadScraper();
        List<BikeSummary> giantRoadBikes = giantRoadScraper.fetchModels();

        System.out.println("Found " + giantRoadBikes.size() + " bikes");

        for (BikeSummary bikeSummary : giantRoadBikes) {
            // fetch geometry
            List<BikeGeometry> currGiantGeometries = giantRoadScraper.fetchGeometry(bikeSummary);
            allBikes.put(bikeSummary, currGiantGeometries);
        }
        int giantBikes = allBikes.size() - cannondaleBikes;
        System.out.println("Found " + giantBikes + " bikes");

        SpecializedRoadScraper specializedRoadScraper = new SpecializedRoadScraper();
        List<BikeSummary> specializedRoadBikes = specializedRoadScraper.fetchModels();

        System.out.println("Found " + specializedRoadBikes.size() + " + Specialized bikes.");

        for (BikeSummary bikeSummary : specializedRoadBikes) {
            List<BikeGeometry> currSpecializedGeometry = specializedRoadScraper.fetchGeometry(bikeSummary);
            allBikes.put(bikeSummary, currSpecializedGeometry);
        }

        int specializedRoadBikesSize = allBikes.size();
        System.out.println("Found " + specializedRoadBikesSize + " + Specialized bikes with geometries.");

        TrekRoadScraper trekRoadScraper = new TrekRoadScraper();
        List<BikeSummary> trekRoadBikes = trekRoadScraper.fetchModels();
        for (BikeSummary bikeSummary : trekRoadBikes) {
            List<BikeGeometry> bikeGeometryList = trekRoadScraper.fetchGeometry(bikeSummary);
            allBikes.put(bikeSummary, bikeGeometryList);
        }

        System.out.println("Found " + allBikes.size() + " Trek bikes.");
	}
}
