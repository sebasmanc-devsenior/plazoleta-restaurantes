package com.plazoleta.application.port.output;

import java.util.Optional;

import com.plazoleta.domain.model.Restaurant;

public interface RestaurantRepositoryPort {
    Restaurant save(Restaurant restaurant);
    Optional<Restaurant> findById(Long id);
}
