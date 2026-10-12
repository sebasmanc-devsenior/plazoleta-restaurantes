package com.plazoleta.infrastructure.adapters.output.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DishJpaRepository extends JpaRepository<DishEntity, Long> {
}
