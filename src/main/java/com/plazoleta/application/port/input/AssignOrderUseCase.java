package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.OrderResponse;

/** HU-13: el empleado se asigna un pedido de su restaurante y lo pasa a EN_PREPARACION. */
public interface AssignOrderUseCase {
    OrderResponse execute(Long orderId, Long employeeId, Long employeeRestaurantId);
}
