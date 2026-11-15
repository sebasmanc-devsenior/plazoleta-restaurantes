package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.RestaurantResponse;

public interface GetRestaurantPort {
    RestaurantResponse execute(Long restaurantId);
}
