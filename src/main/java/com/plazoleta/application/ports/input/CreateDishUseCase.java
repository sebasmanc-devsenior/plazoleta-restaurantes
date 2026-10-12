package com.plazoleta.application.ports.input;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;

public interface CreateDishUseCase {
    DishResponse execute(CreateDishCommand command, Long authenticatedOwnerId);
}
