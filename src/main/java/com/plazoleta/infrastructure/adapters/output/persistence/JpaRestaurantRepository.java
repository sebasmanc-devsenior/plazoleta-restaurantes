package com.plazoleta.infrastructure.adapters.output.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.domain.models.Restaurant;
import com.plazoleta.infrastructure.adapters.output.persistence.jpa.RestaurantEntity;
import com.plazoleta.infrastructure.adapters.output.persistence.jpa.RestaurantJpaRepository;

@Repository
public class JpaRestaurantRepository implements RestaurantRepositoryPort {
    private final RestaurantJpaRepository restaurantJpaRepository;

    public JpaRestaurantRepository(RestaurantJpaRepository restaurantJpaRepository) {
        this.restaurantJpaRepository = restaurantJpaRepository;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantEntity entity = RestaurantEntity.fromDomain(restaurant);
        return restaurantJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantJpaRepository.findById(id).map(RestaurantEntity::toDomain);
    }
}
