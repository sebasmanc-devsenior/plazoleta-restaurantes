package com.plazoleta.application.ports.output;

import java.util.Optional;

import com.plazoleta.domain.models.Dish;

public interface DishRepositoryPort {
    Dish save(Dish dish);
    Optional<Dish> findById(Long id);
}
