package com.plazoleta.application.ports.input;

import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.dto.DishResponse;

public interface UpdateDishUseCase {
    DishResponse execute(Long dishId, UpdateDishCommand command, Long authenticatedOwnerId);
}
