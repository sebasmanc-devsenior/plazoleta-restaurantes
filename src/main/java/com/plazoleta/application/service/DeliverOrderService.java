package com.plazoleta.application.service;

import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.DeliverOrderUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.model.Order;

public class DeliverOrderService implements DeliverOrderUseCase {
    private final OrderRepositoryPort orderRepositoryPort;

    public DeliverOrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
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
        order.deliver(command.getPin());
        return OrderResponse.from(orderRepositoryPort.save(order));
    }
}
