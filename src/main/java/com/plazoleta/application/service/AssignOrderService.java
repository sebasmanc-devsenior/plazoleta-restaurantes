package com.plazoleta.application.service;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.AssignOrderUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public class AssignOrderService implements AssignOrderUseCase {
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;

    public AssignOrderService(OrderRepositoryPort orderRepositoryPort, OrderTraceabilityPort orderTraceabilityPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderTraceabilityPort = orderTraceabilityPort;
    }

    @Override
    public OrderResponse execute(Long orderId, Long employeeId, Long employeeRestaurantId) {
        Order order = findOrderOfEmployeeRestaurant(orderRepositoryPort, orderId, employeeRestaurantId);
        OrderStatus previousStatus = order.getStatus();
        order.assignTo(employeeId);
        // Solo se guarda si el pedido sigue PENDIENTE en la base: si dos empleados compiten, gana uno.
        if (!orderRepositoryPort.updateIfStatus(order, previousStatus)) {
            throw new InvalidOrderStatusException(
                    "The order was just taken or cancelled; only PENDIENTE orders can be assigned");
        }
        orderTraceabilityPort.record(order, previousStatus);
        return OrderResponse.from(order);
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

    /** Desde que un empleado se asigna el pedido, solo él puede avanzarlo (HU-14 y HU-15). */
    static void requireAssignedEmployee(Order order, Long employeeId) {
        if (order.getEmployeeId() != null && !order.getEmployeeId().equals(employeeId)) {
            throw new ForbiddenOperationException("The order is assigned to another employee");
        }
    }
}
