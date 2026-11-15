package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.dto.RestaurantResponse;

/**
 * Handler de restaurantes (HU 2 y 9, y la consulta interna): lo que los controladores le piden a la capa de
 * aplicación. No tiene reglas de negocio, solo delega en los casos de uso por sus interfaces (puertos de entrada).
 */
public interface IRestaurantHandler {
    RestaurantResponse createRestaurant(CreateRestaurantCommand command);

    PageResult<RestaurantListItem> listRestaurants(int page, int size);

    RestaurantResponse getRestaurant(Long restaurantId);
}
