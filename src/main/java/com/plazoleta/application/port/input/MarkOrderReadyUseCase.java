package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.OrderResponse;

/** HU-14: el empleado asignado marca el pedido como listo y el cliente recibe un SMS con el PIN para reclamarlo. */
public interface MarkOrderReadyUseCase {
    OrderResponse execute(Long orderId, Long employeeId, Long employeeRestaurantId);
}
