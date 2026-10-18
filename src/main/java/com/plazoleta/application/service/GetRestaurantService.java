package com.plazoleta.application.service;

import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.port.input.GetRestaurantUseCase;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.exception.RestaurantNotFoundException;

/**
 * Consulta un restaurante por id. Lo usa users-service para verificar que quien crea un
 * empleado (HU-6) es el propietario del restaurante.
 */
public class GetRestaurantService implements GetRestaurantUseCase {
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public GetRestaurantService(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public RestaurantResponse execute(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant id is required");
        }
        var restaurant = restaurantRepositoryPort.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant was not found"));
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getNit(),
                restaurant.getAddress(), restaurant.getPhone(), restaurant.getLogoUrl(), restaurant.getOwnerUserId());
    }
}
