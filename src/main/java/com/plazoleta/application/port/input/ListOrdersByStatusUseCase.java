package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;

/** HU-12: el empleado lista los pedidos de su restaurante filtrando por estado, paginados. */
public interface ListOrdersByStatusUseCase {
    PageResult<OrderResponse> execute(Long employeeRestaurantId, String status, int page, int size);
}
