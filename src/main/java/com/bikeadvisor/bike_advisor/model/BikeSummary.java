package com.bikeadvisor.bike_advisor.model;

public class BikeSummary {
    private String brand;           // Canonical brand name, e.g. "Specialized"
    private String model;           // e.g. "Tarmac SL8"
    private Integer modelYear;      // nullable if brand doesn’t expose
    private Discipline discipline;
    private String wheelSize;
    private String productUrl;
    private String priceText;
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

    public String getWheelSize() { return wheelSize; }
    public void setWheelSize(String wheelSize) { this.wheelSize = wheelSize; }

    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }

    public String getPriceText() { return priceText; }
    public void setPriceText(String priceText) { this.priceText = priceText; }

    public String getGeometryKey() { return geometryKey; }
    public void setGeometryKey(String geometryKey) { this.geometryKey = geometryKey; }
}
