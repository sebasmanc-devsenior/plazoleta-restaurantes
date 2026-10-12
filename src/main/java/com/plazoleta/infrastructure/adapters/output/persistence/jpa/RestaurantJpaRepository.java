package com.plazoleta.infrastructure.adapters.output.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {
    Page<RestaurantEntity> findAllByOrderByNameAsc(Pageable pageable);
}
