package com.bikeadvisor.bike_advisor.export;

import com.bikeadvisor.bike_advisor.model.BikeGeometry;
import com.bikeadvisor.bike_advisor.model.BikeSummary;
import com.bikeadvisor.bike_advisor.model.RideCharacter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class CsvExporter {

    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    private static final CSVFormat BIKE_SUMMARY_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("id", "brand", "model", "discipline", "modelYear",
                       "priceText", "productUrl", "geometryKey")
            .build();

    private static final CSVFormat RIDE_CHARACTER_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("geometryKey", "sizeLabel",
                       "stabilityIndex", "agilityIndex", "comfortIndex", "aggressionIndex", "handlingIndex",
                       "stabilityZIndex", "agilityZIndex", "comfortZIndex", "aggressionZIndex", "handlingZIndex")
            .build();

    private static final CSVFormat GEOMETRY_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("bikeGeometryKey", "sizeLabel", "wheelSize", "reach", "stack", "topTubeEffective",
                       "headTubeAngle", "seatTubeAngleEffective", "headTubeLength", "seatTubeLength",
                       "chainstay", "wheelbase", "bbDrop", "forkOffset", "trail", "standover")
            .build();

    public void writeRideCharacters(List<RideCharacter> rideCharacters, Path outputPath) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), RIDE_CHARACTER_FORMAT)) {
            for (RideCharacter rc : rideCharacters) {
                printer.printRecord(
                        rc.getGeometryKey(),
                        rc.getSizeLabel(),
                        rc.getStabilityIndex(),
                        rc.getAgilityIndex(),
                        rc.getComfortIndex(),
                        rc.getAggressionIndex(),
                        rc.getHandlingIndex(),
                        rc.getStabilityZIndex(),
                        rc.getAgilityZIndex(),
                        rc.getComfortZIndex(),
                        rc.getAggressionZIndex(),
                        rc.getHandlingZIndex()
                );
            }
        }
        log.info("Wrote {} ride character rows to {}", rideCharacters.size(), outputPath);
    }

    public void writeBikeSummaries(List<BikeSummary> bikes, Path outputPath) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), BIKE_SUMMARY_FORMAT)) {
            for (BikeSummary bike : bikes) {
                printer.printRecord(
                        bike.getId(),
                        bike.getBrand(),
                        bike.getModel(),
                        bike.getDiscipline() != null ? bike.getDiscipline().name() : null,
                        bike.getModelYear(),
                        bike.getPriceText(),
                        bike.getProductUrl(),
                        bike.getGeometryKey()
                );
            }
        }
        log.info("Wrote {} bike summaries to {}", bikes.size(), outputPath);
    }

    public void writeGeometries(List<BikeGeometry> geometries, Path outputPath) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath.toFile()), GEOMETRY_FORMAT)) {
            for (BikeGeometry geo : geometries) {
                printer.printRecord(
                        geo.getBikeGeometryKey(),
                        geo.getSizeLabel(),
                        geo.getWheelSize(),
                        geo.getReach(),
                        geo.getStack(),
                        geo.getTopTubeEffective(),
                        geo.getHeadTubeAngle(),
                        geo.getSeatTubeAngleEffective(),
                        geo.getHeadTubeLength(),
                        geo.getSeatTubeLength(),
                        geo.getChainstay(),
                        geo.getWheelbase(),
                        geo.getBbDrop(),
                        geo.getForkOffset(),
                        geo.getTrail(),
                        geo.getStandover()
                );
            }
        }
        log.info("Wrote {} geometry rows to {}", geometries.size(), outputPath);
    }
}
