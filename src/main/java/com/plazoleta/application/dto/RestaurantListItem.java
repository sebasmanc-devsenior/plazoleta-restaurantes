package com.plazoleta.application.dto;

public class RestaurantListItem {
    private final String name;
    private final String logoUrl;

    public RestaurantListItem(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

    public String getName() { return name; }
    public String getLogoUrl() { return logoUrl; }
}
