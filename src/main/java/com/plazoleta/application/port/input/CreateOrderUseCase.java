package com.plazoleta.application.port.input;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderResponse;

/** HU-11: el cliente realiza un pedido a un restaurante. */
public interface CreateOrderUseCase {
    OrderResponse execute(CreateOrderCommand command, Long clientId);
}
