package com.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.infrastructure.input.rest.dto.CreateDishRequest;
import com.plazoleta.infrastructure.input.rest.dto.UpdateDishRequest;

/** Convierte los requests HTTP de platos (ya validados con Jakarta) en comandos de la capa de aplicación. */
public final class DishRequestMapper {
    private DishRequestMapper() {
    }

    public static CreateDishCommand toCommand(CreateDishRequest request) {
        return new CreateDishCommand(request.name(), request.price(), request.description(), request.imageUrl(),
                request.category(), request.restaurantId());
    }

    public static UpdateDishCommand toCommand(UpdateDishRequest request) {
        return new UpdateDishCommand(request.price(), request.description());
    }
}
