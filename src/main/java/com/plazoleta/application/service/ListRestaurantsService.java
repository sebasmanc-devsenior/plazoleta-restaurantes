package com.plazoleta.application.service;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.port.input.ListRestaurantsUseCase;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;

public class ListRestaurantsService implements ListRestaurantsUseCase {
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public ListRestaurantsService(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public PageResult<RestaurantListItem> execute(int page, int size) {
        validatePage(page, size);
        var result = restaurantRepositoryPort.findAll(page, size);
        return new PageResult<>(result.getContent().stream()
                .map(restaurant -> new RestaurantListItem(restaurant.getName(), restaurant.getLogoUrl()))
                .toList(), page, size, result.getTotalElements());
    }

    static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("La página debe ser 0 o mayor y el tamaño debe estar entre 1 y 100");
        }
    }
}
