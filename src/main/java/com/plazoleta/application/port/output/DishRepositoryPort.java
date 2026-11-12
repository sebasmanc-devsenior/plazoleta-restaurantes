package com.plazoleta.application.port.output;

import java.util.Optional;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.domain.model.Dish;

public interface DishRepositoryPort {
    Dish save(Dish dish);
    Optional<Dish> findById(Long id);
    /** HU-10: platos de un restaurante, habilitados y deshabilitados; category null = todas las categorías. */
    PageResult<Dish> findByRestaurant(Long restaurantId, String category, int page, int size);
}
