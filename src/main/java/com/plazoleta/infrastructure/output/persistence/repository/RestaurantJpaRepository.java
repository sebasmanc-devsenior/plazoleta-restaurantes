package com.plazoleta.infrastructure.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.plazoleta.infrastructure.output.persistence.entity.RestaurantEntity;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {
}
