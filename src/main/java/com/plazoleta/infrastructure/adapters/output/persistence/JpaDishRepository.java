package com.plazoleta.infrastructure.adapters.output.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plazoleta.application.ports.output.DishRepositoryPort;
import com.plazoleta.domain.models.Dish;
import com.plazoleta.infrastructure.adapters.output.persistence.jpa.DishEntity;
import com.plazoleta.infrastructure.adapters.output.persistence.jpa.DishJpaRepository;

@Repository
public class JpaDishRepository implements DishRepositoryPort {
    private final DishJpaRepository dishJpaRepository;

    public JpaDishRepository(DishJpaRepository dishJpaRepository) {
        this.dishJpaRepository = dishJpaRepository;
    }

    @Override
    public Dish save(Dish dish) {
        DishEntity entity = DishEntity.fromDomain(dish);
        return dishJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Dish> findById(Long id) {
        return dishJpaRepository.findById(id).map(DishEntity::toDomain);
    }
}
