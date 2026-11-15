package com.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.infrastructure.input.rest.dto.CreateRestaurantRequest;

/** Convierte el request HTTP (ya validado con Jakarta) en el comando de la capa de aplicación. */
public final class RestaurantRequestMapper {
    private RestaurantRequestMapper() {
    }

    public static CreateRestaurantCommand toCommand(CreateRestaurantRequest request) {
        return new CreateRestaurantCommand(request.name(), request.nit(), request.address(), request.phone(),
                request.logoUrl(), request.ownerUserId());
    }
}
