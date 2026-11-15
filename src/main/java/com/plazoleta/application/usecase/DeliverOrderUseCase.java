package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.DeliverOrderPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public class DeliverOrderUseCase implements DeliverOrderPort {
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;

    public DeliverOrderUseCase(OrderRepositoryPort orderRepositoryPort, OrderTraceabilityPort orderTraceabilityPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderTraceabilityPort = orderTraceabilityPort;
    }

    @Override
    public OrderResponse execute(Long orderId, DeliverOrderCommand command, Long employeeId,
                                 Long employeeRestaurantId) {
        if (command == null) {
            throw new IllegalArgumentException(MensajesError.CUERPO_DE_LA_PETICION_ES_OBLIGATORIO);
        }
        Order order = AssignOrderUseCase.findOrderOfEmployeeRestaurant(orderRepositoryPort, orderId,
                employeeRestaurantId);
        AssignOrderUseCase.requireAssignedEmployee(order, employeeId);
        OrderStatus previousStatus = order.getStatus();
        order.deliver(command.getPin());
        // Solo se guarda si el pedido sigue LISTO en la base: una entrega doble no se registra dos veces.
        if (!orderRepositoryPort.updateIfStatus(order, previousStatus)) {
            throw new InvalidOrderStatusException(MensajesError.PEDIDO_ACABA_DE_CAMBIAR_SOLO_SE_ENTREGAN);
        }
        orderTraceabilityPort.record(order, previousStatus);
        return OrderResponse.from(order);
    }
}
