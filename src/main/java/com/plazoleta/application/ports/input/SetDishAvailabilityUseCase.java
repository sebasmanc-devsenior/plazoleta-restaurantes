package com.plazoleta.application.ports.input;

import com.plazoleta.application.dto.DishResponse;

public interface SetDishAvailabilityUseCase {
    DishResponse execute(Long dishId, boolean active, Long authenticatedOwnerId);
}
