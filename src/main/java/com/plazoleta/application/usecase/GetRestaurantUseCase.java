package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.port.input.GetRestaurantPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.exception.RestaurantNotFoundException;

/**
 * Consulta un restaurante por id. Lo usa users-service para verificar que quien crea un
 * empleado (HU-6) es el propietario del restaurante.
 */
public class GetRestaurantUseCase implements GetRestaurantPort {
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public GetRestaurantUseCase(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public RestaurantResponse execute(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException(MensajesError.ID_DEL_RESTAURANTE_ES_OBLIGATORIO);
        }
        var restaurant = restaurantRepositoryPort.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(MensajesError.RESTAURANTE_NO_EXISTE));
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getNit(),
                restaurant.getAddress(), restaurant.getPhone(), restaurant.getLogoUrl(), restaurant.getOwnerUserId());
    }
}
