package com.bikeadvisor.bike_advisor.export;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.Discipline;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvReader {

    private static final Logger log = LoggerFactory.getLogger(CsvReader.class);

    private static final CSVFormat GEOMETRY_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    public List<BikeSummary> readBikes(Path inputPath) throws IOException {
        List<BikeSummary> bikes = new ArrayList<>();

        try (var parser = GEOMETRY_FORMAT.parse(new FileReader(inputPath.toFile()))) {
            for (CSVRecord record : parser) {
                BikeSummary bike = new BikeSummary();
                bike.setBrand(record.get("brand"));
                bike.setModel(record.get("model"));
                bike.setProductUrl(record.get("productUrl"));
                bike.setGeometryKey(record.get("geometryKey"));
                String priceStr = getOptionalString(record, "price");
                if (priceStr != null) {
                    try { bike.setPrice(Double.parseDouble(priceStr)); } catch (NumberFormatException ignored) {}
                }

                String disciplineStr = getOptionalString(record, "discipline");
                if (disciplineStr != null) {
                    try { bike.setDiscipline(Discipline.valueOf(disciplineStr)); }
                    catch (IllegalArgumentException ignored) {}
                }

                String modelYearStr = getOptionalString(record, "modelYear");
                if (modelYearStr != null) {
                    try { bike.setModelYear(Integer.parseInt(modelYearStr)); }
                    catch (NumberFormatException ignored) {}
                }

                bikes.add(bike);
            }
        }

        log.info("Read {} bikes from {}", bikes.size(), inputPath);
        return bikes;
    }

    public List<BikeGeometry> readGeometries(Path inputPath) throws IOException {
        List<BikeGeometry> geometries = new ArrayList<>();

        try (var parser = GEOMETRY_FORMAT.parse(new FileReader(inputPath.toFile()))) {
            for (CSVRecord record : parser) {
                BikeGeometry geo = new BikeGeometry();
                geo.setBikeGeometryKey(record.get("bikeGeometryKey"));
                geo.setSizeLabel(record.get("sizeLabel"));
                geo.setWheelSize(getOptionalString(record, "wheelSize"));
                geo.setReach(parseDouble(record.get("reach")));
                geo.setStack(parseDouble(record.get("stack")));
                geo.setTopTubeEffective(parseDouble(record.get("topTubeEffective")));
                geo.setHeadTubeAngle(parseDouble(record.get("headTubeAngle")));
                geo.setSeatTubeAngleEffective(parseDouble(record.get("seatTubeAngleEffective")));
                geo.setHeadTubeLength(parseDouble(record.get("headTubeLength")));
                geo.setSeatTubeLength(parseDouble(record.get("seatTubeLength")));
                geo.setChainstay(parseDouble(record.get("chainstay")));
                geo.setWheelbase(parseDouble(record.get("wheelbase")));
                geo.setBbDrop(parseDouble(record.get("bbDrop")));
                geo.setForkOffset(parseDouble(record.get("forkOffset")));
                geo.setTrail(parseDouble(record.get("trail")));
                geo.setStandover(parseDouble(record.get("standover")));
                geometries.add(geo);
            }
        }

        log.info("Read {} geometry rows from {}", geometries.size(), inputPath);
        return geometries;
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        return Double.parseDouble(value);
    }

    // Returns null if the column doesn't exist in the CSV (e.g. files written before this field was added)
    private static String getOptionalString(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return (value == null || value.isBlank()) ? null : value;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
