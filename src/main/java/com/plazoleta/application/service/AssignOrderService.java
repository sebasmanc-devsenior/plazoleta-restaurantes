package com.plazoleta.application.service;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.AssignOrderUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;

public class AssignOrderService implements AssignOrderUseCase {
    private final OrderRepositoryPort orderRepositoryPort;

    public AssignOrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public OrderResponse execute(Long orderId, Long employeeId, Long employeeRestaurantId) {
        Order order = findOrderOfEmployeeRestaurant(orderRepositoryPort, orderId, employeeRestaurantId);
        order.assignTo(employeeId);
        return OrderResponse.from(orderRepositoryPort.save(order));
    }

    /** Busca el pedido y comprueba que sea del restaurante donde trabaja el empleado (HU-13 a HU-15). */
    static Order findOrderOfEmployeeRestaurant(OrderRepositoryPort orders, Long orderId, Long employeeRestaurantId) {
        if (employeeRestaurantId == null) {
            throw new ForbiddenOperationException("The employee is not linked to a restaurant");
        }
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order was not found"));
        if (!order.getRestaurantId().equals(employeeRestaurantId)) {
            throw new ForbiddenOperationException("The order belongs to another restaurant");
        }
        return order;
    }
}
