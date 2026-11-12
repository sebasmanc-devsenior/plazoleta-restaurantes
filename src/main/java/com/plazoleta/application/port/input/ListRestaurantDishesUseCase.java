package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;

/** HU-10: el cliente lista el menú de un restaurante, paginado y con filtro opcional por categoría. */
public interface ListRestaurantDishesUseCase {
    PageResult<DishResponse> execute(Long restaurantId, String category, int page, int size);
}
