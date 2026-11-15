package com.plazoleta.infrastructure.output.persistence.mapper;

import com.plazoleta.domain.model.Dish;
import com.plazoleta.infrastructure.output.persistence.entity.DishEntity;

/** Convierte entre el modelo del dominio Dish y la entidad JPA de la tabla dishes. */
public final class DishEntityMapper {
    private DishEntityMapper() {
    }

    public static DishEntity toEntity(Dish dish) {
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

    public static Dish toDomain(DishEntity entity) {
        return new Dish(entity.getId(), entity.getName(), entity.getPrice(), entity.getDescription(),
                entity.getImageUrl(), entity.getCategory(), entity.getRestaurantId(), entity.isActive());
    }
}
