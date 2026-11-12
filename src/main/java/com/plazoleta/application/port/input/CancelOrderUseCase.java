package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.OrderResponse;

/** HU-16: el cliente cancela su propio pedido mientras siga PENDIENTE. */
public interface CancelOrderUseCase {
    OrderResponse execute(Long orderId, Long clientId);
}
