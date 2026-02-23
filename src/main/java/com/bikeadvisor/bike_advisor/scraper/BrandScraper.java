package com.bikeadvisor.bike_advisor.scraper;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;

import java.util.List;

public interface BrandScraper {
    String getBrandName();

    List<BikeSummary> fetchModels() throws Exception;

    List<BikeGeometry> fetchGeometry(BikeSummary model) throws Exception;
}
