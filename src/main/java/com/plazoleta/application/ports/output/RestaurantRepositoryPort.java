package com.plazoleta.application.ports.output;

import java.util.Optional;

import com.plazoleta.domain.models.Restaurant;

public interface RestaurantRepositoryPort {
    Restaurant save(Restaurant restaurant);
    Optional<Restaurant> findById(Long id);
}
