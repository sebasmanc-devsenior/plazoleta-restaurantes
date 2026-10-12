package com.plazoleta.application.dto;

public class UpdateDishCommand {
    private Integer price;
    private String description;

    public UpdateDishCommand() {}

    public UpdateDishCommand(Integer price, String description) {
        this.price = price;
        this.description = description;
    }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
