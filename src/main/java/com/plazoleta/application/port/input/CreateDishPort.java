package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;

public interface CreateDishPort {
    DishResponse execute(CreateDishCommand command, Long authenticatedOwnerId);
}
