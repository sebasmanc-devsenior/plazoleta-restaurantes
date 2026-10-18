package com.plazoleta.application.port.output;

import java.util.Optional;

import com.plazoleta.domain.model.Dish;

public interface DishRepositoryPort {
    Dish save(Dish dish);
    Optional<Dish> findById(Long id);
}
