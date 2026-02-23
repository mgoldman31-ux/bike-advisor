package com.bikeadvisor.bike_advisor.model;

public enum Brand {
    CANNONDALE("Cannondale"),
    GIANT("Giant"),
    TREK("Trek"),
    SPECIALIZED("Specialized");

    private final String brand;
    Brand(String brand) {
        this.brand = brand;
    }

    public String getBrandName() { return this.brand; }


}
