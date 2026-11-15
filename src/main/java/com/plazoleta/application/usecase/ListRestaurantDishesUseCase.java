package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.ListRestaurantDishesPort;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.exception.RestaurantNotFoundException;

public class ListRestaurantDishesUseCase implements ListRestaurantDishesPort {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public ListRestaurantDishesUseCase(DishRepositoryPort dishRepositoryPort,
                                       RestaurantRepositoryPort restaurantRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public PageResult<DishResponse> execute(Long restaurantId, String category, int page, int size) {
        ListRestaurantsUseCase.validatePage(page, size);
        if (restaurantId == null) {
            throw new IllegalArgumentException(MensajesError.ID_DEL_RESTAURANTE_ES_OBLIGATORIO);
        }
        if (restaurantRepositoryPort.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFoundException(MensajesError.RESTAURANTE_NO_EXISTE);
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
