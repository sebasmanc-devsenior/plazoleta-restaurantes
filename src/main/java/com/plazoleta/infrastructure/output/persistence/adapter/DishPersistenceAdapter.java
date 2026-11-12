package com.plazoleta.infrastructure.output.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.plazoleta.application.dto.PageResult;
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

    /** HU-10: todos los platos del restaurante, ordenados por nombre, con filtro opcional por categoría. */
    @Override
    public PageResult<Dish> findByRestaurant(Long restaurantId, String category, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        var result = category == null
                ? dishJpaRepository.findByRestaurant_Id(restaurantId, pageable)
                : dishJpaRepository.findByRestaurant_IdAndCategoryIgnoreCase(restaurantId, category, pageable);
        return new PageResult<>(result.getContent().stream().map(DishEntity::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }
}
