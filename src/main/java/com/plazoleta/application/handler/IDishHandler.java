package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.UpdateDishCommand;

/**
 * Handler de platos (HU 3, 4, 7 y 10). No tiene reglas de negocio, solo delega en los casos de uso por sus
 * interfaces. El id del propietario lo pone el controlador a partir del token, nunca del cuerpo.
 */
public interface IDishHandler {
    DishResponse createDish(CreateDishCommand command, Long ownerId);

    DishResponse updateDish(Long dishId, UpdateDishCommand command, Long ownerId);

    DishResponse setDishAvailability(Long dishId, boolean active, Long ownerId);

    PageResult<DishResponse> listRestaurantDishes(Long restaurantId, String category, int page, int size);
}
