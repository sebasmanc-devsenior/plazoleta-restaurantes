package com.plazoleta.application.ports.input;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.RestaurantResponse;

public interface CreateRestaurantUseCase {
    RestaurantResponse execute(CreateRestaurantCommand command);
}
