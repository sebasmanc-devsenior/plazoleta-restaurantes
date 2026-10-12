package com.plazoleta.application.ports.output;

import java.util.Optional;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.domain.models.Restaurant;

public interface RestaurantRepositoryPort {
    Restaurant save(Restaurant restaurant);
    Optional<Restaurant> findById(Long id);
    PageResult<Restaurant> findAll(int page, int size);
}
