package com.plazoleta.domain.model;

import java.util.Objects;

import com.plazoleta.domain.exception.InvalidPriceException;

public class Dish {
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private String category;
    private Long restaurantId;
    private boolean active;

    public Dish() {
    }

    public Dish(Long id, String name, Integer price, String description, String imageUrl, String category, Long restaurantId, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.restaurantId = restaurantId;
        this.active = active;
        validate();
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Dish name is required");
        }
        if (price == null || price <= 0) {
            throw new InvalidPriceException("Price must be an integer greater than 0");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant is required");
        }
    }

    public void updatePriceAndDescription(Integer newPrice, String newDescription) {
        if (newPrice == null || newPrice <= 0) {
            throw new InvalidPriceException("Price must be greater than 0");
        }
        if (newDescription == null || newDescription.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        this.price = newPrice;
        this.description = newDescription;
    }

    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
