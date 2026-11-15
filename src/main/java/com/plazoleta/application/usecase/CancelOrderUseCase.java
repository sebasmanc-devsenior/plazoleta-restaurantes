package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.CancelOrderPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public class CancelOrderUseCase implements CancelOrderPort {
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;

    public CancelOrderUseCase(OrderRepositoryPort orderRepositoryPort, OrderTraceabilityPort orderTraceabilityPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderTraceabilityPort = orderTraceabilityPort;
    }

    @Override
    public OrderResponse execute(Long orderId, Long clientId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(MensajesError.PEDIDO_NO_EXISTE));
        // Un cliente solo puede cancelar sus propios pedidos.
        if (!order.getClientId().equals(clientId)) {
            throw new ForbiddenOperationException(MensajesError.PEDIDO_PERTENECE_A_OTRO_CLIENTE);
        }
        OrderStatus previousStatus = order.getStatus();
        order.cancel();
        // Solo se guarda si el pedido sigue PENDIENTE en la base: si un empleado lo tomó en ese instante,
        // el cliente recibe el mismo mensaje del criterio de aceptación.
        if (!orderRepositoryPort.updateIfStatus(order, previousStatus)) {
            throw new InvalidOrderStatusException(Order.CANNOT_CANCEL_MESSAGE);
        }
        orderTraceabilityPort.record(order, previousStatus);
        return OrderResponse.from(order);
    }
}
