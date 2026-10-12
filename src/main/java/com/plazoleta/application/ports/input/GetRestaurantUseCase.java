package com.plazoleta.application.ports.input;

import com.plazoleta.application.dto.RestaurantResponse;

public interface GetRestaurantUseCase {
    RestaurantResponse execute(Long restaurantId);
}
