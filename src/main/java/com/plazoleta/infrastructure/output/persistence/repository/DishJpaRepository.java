package com.plazoleta.infrastructure.output.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.plazoleta.infrastructure.output.persistence.entity.DishEntity;

public interface DishJpaRepository extends JpaRepository<DishEntity, Long> {
    Page<DishEntity> findByRestaurant_Id(Long restaurantId, Pageable pageable);
    Page<DishEntity> findByRestaurant_IdAndCategoryIgnoreCase(Long restaurantId, String category, Pageable pageable);
}
