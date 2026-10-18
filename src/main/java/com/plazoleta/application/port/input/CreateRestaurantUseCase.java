package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.RestaurantResponse;

public interface CreateRestaurantUseCase {
    RestaurantResponse execute(CreateRestaurantCommand command);
}
