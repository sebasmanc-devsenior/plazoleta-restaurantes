package com.plazoleta.infrastructure.output.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.infrastructure.output.persistence.entity.DishEntity;
import com.plazoleta.infrastructure.output.persistence.repository.DishJpaRepository;

@Repository
public class DishPersistenceAdapter implements DishRepositoryPort {
    private final DishJpaRepository dishJpaRepository;

    public DishPersistenceAdapter(DishJpaRepository dishJpaRepository) {
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
