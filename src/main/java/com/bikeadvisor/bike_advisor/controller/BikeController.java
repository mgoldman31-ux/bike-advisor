package com.bikeadvisor.bike_advisor.controller;

import com.bikeadvisor.bike_advisor.dto.FilterOptions;
import com.bikeadvisor.bike_advisor.dto.ScatterPoint;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.repository.BikeGeometryRepository;
import com.bikeadvisor.bike_advisor.repository.BikeSummaryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final BikeSummaryRepository bikeSummaryRepository;
    private final BikeGeometryRepository bikeGeometryRepository;

    public BikeController(BikeSummaryRepository bikeSummaryRepository,
                          BikeGeometryRepository bikeGeometryRepository) {
        this.bikeSummaryRepository = bikeSummaryRepository;
        this.bikeGeometryRepository = bikeGeometryRepository;
    }

    /** Scatter plot data: one point per (geometry, size). Filterable by brand, discipline, wheelSize. */
    @GetMapping("/scatter")
    public List<ScatterPoint> getScatterData(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String wheelSize) {
        return bikeGeometryRepository.findScatterData(brand, discipline, wheelSize);
    }

    /** Filtered bike list. All params optional — omitting a param means no filter on that field. */
    @GetMapping
    public List<BikeSummary> getBikes(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return bikeSummaryRepository.findByFilters(brand, discipline, search, minPrice, maxPrice);
    }

    /** Available options for all filter dropdowns. */
    @GetMapping("/filters")
    public FilterOptions getFilters() {
        return new FilterOptions(
                bikeSummaryRepository.findDistinctBrands(),
                bikeSummaryRepository.findDistinctDisciplines(),
                bikeGeometryRepository.findDistinctWheelSizes()
        );
    }
}
