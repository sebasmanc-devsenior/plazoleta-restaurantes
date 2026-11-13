package com.plazoleta.application.service;

import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.DeliverOrderUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public class DeliverOrderService implements DeliverOrderUseCase {
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;

    public DeliverOrderService(OrderRepositoryPort orderRepositoryPort, OrderTraceabilityPort orderTraceabilityPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderTraceabilityPort = orderTraceabilityPort;
    }

    @Override
    public OrderResponse execute(Long orderId, DeliverOrderCommand command, Long employeeId,
                                 Long employeeRestaurantId) {
        if (command == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        Order order = AssignOrderService.findOrderOfEmployeeRestaurant(orderRepositoryPort, orderId,
                employeeRestaurantId);
        AssignOrderService.requireAssignedEmployee(order, employeeId);
        OrderStatus previousStatus = order.getStatus();
        order.deliver(command.getPin());
        Order saved = orderRepositoryPort.save(order);
        orderTraceabilityPort.record(saved, previousStatus);
        return OrderResponse.from(saved);
    }
}
