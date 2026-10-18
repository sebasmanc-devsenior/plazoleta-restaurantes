package com.plazoleta.infrastructure.output.persistence.entity;

import com.plazoleta.domain.model.Dish;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

@Entity
@Table(name = "dishes", indexes = {
        @Index(name = "idx_dishes_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_dishes_restaurant_active", columnList = "restaurant_id,active")
})
public class DishEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dishes_restaurant"))
    private RestaurantEntity restaurant;

    @Column(nullable = false)
    private boolean active;

    public DishEntity() {
    }

    public static DishEntity fromDomain(Dish dish) {
        DishEntity entity = new DishEntity();
        entity.setId(dish.getId());
        entity.setName(dish.getName());
        entity.setPrice(dish.getPrice());
        entity.setDescription(dish.getDescription());
        entity.setImageUrl(dish.getImageUrl());
        entity.setCategory(dish.getCategory());
        entity.setRestaurantId(dish.getRestaurantId());
        entity.setActive(dish.isActive());
        return entity;
    }

    public Dish toDomain() {
        return new Dish(id, name, price, description, imageUrl, category, restaurant.getId(), active);
    }

    static DishEntity reference(Long id) {
        DishEntity entity = new DishEntity();
        entity.setId(id);
        return entity;
    }
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
    public Long getRestaurantId() { return restaurant == null ? null : restaurant.getId(); }
    public void setRestaurantId(Long restaurantId) {
        restaurant = restaurantId == null ? null : RestaurantEntity.reference(restaurantId);
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
