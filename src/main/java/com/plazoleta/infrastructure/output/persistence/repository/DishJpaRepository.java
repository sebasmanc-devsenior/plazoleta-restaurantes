package com.plazoleta.infrastructure.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.plazoleta.infrastructure.output.persistence.entity.DishEntity;

public interface DishJpaRepository extends JpaRepository<DishEntity, Long> {
}
