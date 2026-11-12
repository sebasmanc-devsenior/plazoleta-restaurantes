package com.plazoleta.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.plazoleta.domain.exception.InvalidOrderStatusException;

/** Pedido de un cliente a un restaurante. */
public class Order {
    private final Long id;
    private final Long clientId;
    private final Long restaurantId;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;
    private Long employeeId;

    public Order(Long id, Long clientId, Long restaurantId, OrderStatus status, LocalDateTime createdAt,
                 List<OrderItem> items) {
        this(id, clientId, restaurantId, status, createdAt, items, null);
    }

    public Order(Long id, Long clientId, Long restaurantId, OrderStatus status, LocalDateTime createdAt,
                 List<OrderItem> items, Long employeeId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client is required");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one dish");
        }
        Set<Long> dishIds = new HashSet<>();
        for (OrderItem item : items) {
            if (!dishIds.add(item.getDishId())) {
                throw new IllegalArgumentException("Each dish can appear only once; use the quantity field instead");
            }
        }
        this.id = id;
        this.clientId = clientId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.createdAt = createdAt;
        this.items = List.copyOf(items);
        this.employeeId = employeeId;
    }

    /** HU-11: todo pedido nuevo queda en estado PENDIENTE. */
    public static Order create(Long clientId, Long restaurantId, List<OrderItem> items, LocalDateTime now) {
        return new Order(null, clientId, restaurantId, OrderStatus.PENDIENTE, now, items);
    }

    /** HU-13: el empleado se asigna el pedido y este pasa de PENDIENTE a EN_PREPARACION. */
    public void assignTo(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee is required");
        }
        if (status != OrderStatus.PENDIENTE) {
            throw new InvalidOrderStatusException("Only PENDIENTE orders can be assigned; this order is " + status);
        }
        this.employeeId = employeeId;
        this.status = OrderStatus.EN_PREPARACION;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Long getRestaurantId() { return restaurantId; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
    public Long getEmployeeId() { return employeeId; }
}
