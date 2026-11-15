package com.plazoleta.infrastructure.output.persistence.mapper;

import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.infrastructure.output.persistence.entity.RestaurantEntity;

/** Convierte entre el modelo del dominio Restaurant y la entidad JPA de la tabla restaurants. */
public final class RestaurantEntityMapper {
    private RestaurantEntityMapper() {
    }

    public static RestaurantEntity toEntity(Restaurant restaurant) {
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

    public static Restaurant toDomain(RestaurantEntity entity) {
        return new Restaurant(entity.getId(), entity.getName(), entity.getNit(), entity.getAddress(), entity.getPhone(),
                entity.getLogoUrl(), entity.getOwnerUserId());
    }
}
