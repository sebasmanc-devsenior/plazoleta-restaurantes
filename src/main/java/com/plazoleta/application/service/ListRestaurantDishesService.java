package com.plazoleta.application.service;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.ListRestaurantDishesUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.exception.RestaurantNotFoundException;

public class ListRestaurantDishesService implements ListRestaurantDishesUseCase {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public ListRestaurantDishesService(DishRepositoryPort dishRepositoryPort,
                                       RestaurantRepositoryPort restaurantRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public PageResult<DishResponse> execute(Long restaurantId, String category, int page, int size) {
        ListRestaurantsService.validatePage(page, size);
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant id is required");
        }
        if (restaurantRepositoryPort.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFoundException("Restaurant was not found");
        }
        String categoryFilter = category == null || category.isBlank() ? null : category.trim();
        // El menú incluye los platos deshabilitados (HU-7): el cliente los ve con active = false,
        // pero no puede pedirlos.
        var result = dishRepositoryPort.findByRestaurant(restaurantId, categoryFilter, page, size);
        return new PageResult<>(result.getContent().stream()
                .map(dish -> new DishResponse(dish.getId(), dish.getName(), dish.getPrice(), dish.getDescription(),
                        dish.getImageUrl(), dish.getCategory(), dish.getRestaurantId(), dish.isActive()))
                .toList(), page, size, result.getTotalElements());
    }
}
