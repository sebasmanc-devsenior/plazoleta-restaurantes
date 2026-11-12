package com.plazoleta.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.plazoleta.domain.model.Order;

public class OrderResponse {
    private final Long id;
    private final Long clientId;
    private final Long restaurantId;
    private final String status;
    private final LocalDateTime createdAt;
    /** HU-13: empleado asignado; null mientras nadie se haya asignado el pedido. */
    private final Long employeeId;
    private final List<OrderItemCommand> dishes;

    public OrderResponse(Long id, Long clientId, Long restaurantId, String status, LocalDateTime createdAt,
                         Long employeeId, List<OrderItemCommand> dishes) {
        this.id = id;
        this.clientId = clientId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.createdAt = createdAt;
        this.employeeId = employeeId;
        this.dishes = List.copyOf(dishes);
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getClientId(), order.getRestaurantId(), order.getStatus().name(),
                order.getCreatedAt(), order.getEmployeeId(), order.getItems().stream()
                        .map(item -> new OrderItemCommand(item.getDishId(), item.getQuantity()))
                        .toList());
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Long getRestaurantId() { return restaurantId; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getEmployeeId() { return employeeId; }
    public List<OrderItemCommand> getDishes() { return dishes; }
}
