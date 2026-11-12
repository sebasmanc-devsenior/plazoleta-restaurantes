package com.plazoleta.application.service;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.CancelOrderUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;

public class CancelOrderService implements CancelOrderUseCase {
    private final OrderRepositoryPort orderRepositoryPort;

    public CancelOrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public OrderResponse execute(Long orderId, Long clientId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order was not found"));
        // Un cliente solo puede cancelar sus propios pedidos.
        if (!order.getClientId().equals(clientId)) {
            throw new ForbiddenOperationException("The order belongs to another client");
        }
        order.cancel();
        return OrderResponse.from(orderRepositoryPort.save(order));
    }
}
