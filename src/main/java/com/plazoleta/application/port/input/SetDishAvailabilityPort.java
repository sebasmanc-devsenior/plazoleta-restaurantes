package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.DishResponse;

public interface SetDishAvailabilityPort {
    DishResponse execute(Long dishId, boolean active, Long authenticatedOwnerId);
}
