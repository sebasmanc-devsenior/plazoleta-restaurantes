package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;

/** HU-15: el empleado asignado entrega el pedido digitando el PIN que recibió el cliente. */
public interface DeliverOrderUseCase {
    OrderResponse execute(Long orderId, DeliverOrderCommand command, Long employeeId, Long employeeRestaurantId);
}
