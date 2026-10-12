package com.plazoleta.infrastructure.adapters.output.persistence.jpa;

import com.plazoleta.domain.models.Restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_owner_user_id", columnList = "owner_user_id"),
        @Index(name = "idx_restaurants_nit", columnList = "nit")
})
public class RestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nit;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String logoUrl;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    public RestaurantEntity() {
    }

    public static RestaurantEntity fromDomain(Restaurant restaurant) {
        RestaurantEntity entity = new RestaurantEntity();
        entity.setId(restaurant.getId());
        entity.setName(restaurant.getName());
        entity.setNit(restaurant.getNit());
        entity.setAddress(restaurant.getAddress());
        entity.setPhone(restaurant.getPhone());
        entity.setLogoUrl(restaurant.getLogoUrl());
        entity.setOwnerUserId(restaurant.getOwnerUserId());
        return entity;
    }

    public Restaurant toDomain() {
        return new Restaurant(id, name, nit, address, phone, logoUrl, ownerUserId);
    }

    static RestaurantEntity reference(Long id) {
        RestaurantEntity entity = new RestaurantEntity();
        entity.setId(id);
        return entity;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
}
