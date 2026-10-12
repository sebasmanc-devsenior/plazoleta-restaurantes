package com.plazoleta.infrastructure.adapters.output.persistence;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.plazoleta.application.dto.PageResult;
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

    /** HU-9: orden alfabético por nombre, paginado según el tamaño pedido. */
    @Override
    public PageResult<Restaurant> findAll(int page, int size) {
        var result = restaurantJpaRepository.findAllByOrderByNameAsc(PageRequest.of(page, size));
        return new PageResult<>(result.getContent().stream().map(RestaurantEntity::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }
}
