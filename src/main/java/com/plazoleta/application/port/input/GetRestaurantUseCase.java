package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.RestaurantResponse;

public interface GetRestaurantUseCase {
    RestaurantResponse execute(Long restaurantId);
}
