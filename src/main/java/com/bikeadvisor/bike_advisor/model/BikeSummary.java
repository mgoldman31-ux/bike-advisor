package com.bikeadvisor.bike_advisor.model;

import java.net.URI;
import java.net.URISyntaxException;

public class BikeSummary {
    private String brand;           // Canonical brand name, e.g. "Specialized"
    private String model;           // e.g. "Tarmac SL8"
    private Integer modelYear;      // nullable if brand doesn’t expose
    private Discipline discipline;
    private String productUrl;
    private Double price;
    private String geometryKey;

    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    /** Strips currency symbols and commas, then parses to Double. Returns null if unparseable.
     *  For price ranges like "$850-900", returns the higher value. */
    public static Double parsePrice(String text) {
        if (text == null) return null;
        String cleaned = text.replaceAll("[$,\\s]", "");
        if (cleaned.isEmpty()) return null;
        if (cleaned.matches("\\d+(\\.\\d+)?-\\d+(\\.\\d+)?")) {
            String[] parts = cleaned.split("-");
            try { return Math.max(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])); }
            catch (NumberFormatException ignored) {}
        }
        String numeric = cleaned.replaceAll("[^\\d.]", "");
        if (numeric.isEmpty()) return null;
        try { return Double.parseDouble(numeric); } catch (NumberFormatException e) { return null; }
    }

    public String getGeometryKey() { return geometryKey; }
    public void setGeometryKey(String geometryKey) { this.geometryKey = geometryKey; }

    private Double stabilityIndex;
    private Double aeroIndex;
    private Double agilityIndex;

    public Double getStabilityIndex() { return stabilityIndex; }
    public void setStabilityIndex(Double stabilityIndex) { this.stabilityIndex = stabilityIndex; }

    public Double getAeroIndex() { return aeroIndex; }
    public void setAeroIndex(Double aeroIndex) { this.aeroIndex = aeroIndex; }

    public Double getAgilityIndex() { return agilityIndex; }
    public void setAgilityIndex(Double agilityIndex) { this.agilityIndex = agilityIndex; }

    /**
     * Unique, human-readable identifier derived from the product URL path.
     * If the last path segment is a 4-digit year, it is combined with the preceding segment
     * to preserve uniqueness across model years.
     * e.g. "cannondale_supersix-evo-1", "cannondale_supersix-evo-1-2025"
     * Falls back to brand + model slug if productUrl is unavailable.
     */
    public String getId() {
        if (productUrl != null) {
            try {
                String path = new URI(productUrl).getPath().replaceAll("/$", "");
                String[] segs = path.split("/");
                String last = segs[segs.length - 1];
                String slug = last.matches("\\d{4}") && segs.length >= 2
                        ? segs[segs.length - 2] + "-" + last
                        : last;
                return brand.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "_" + slug;
            } catch (URISyntaxException ignored) {}
        }
        return (brand + " " + model).toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
