package com.plazoleta.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pedido de un cliente a un restaurante. */
public class Order {
    private final Long id;
    private final Long clientId;
    private final Long restaurantId;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;

    public Order(Long id, Long clientId, Long restaurantId, OrderStatus status, LocalDateTime createdAt,
                 List<OrderItem> items) {
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
    }

    /** HU-11: todo pedido nuevo queda en estado PENDIENTE. */
    public static Order create(Long clientId, Long restaurantId, List<OrderItem> items, LocalDateTime now) {
        return new Order(null, clientId, restaurantId, OrderStatus.PENDIENTE, now, items);
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Long getRestaurantId() { return restaurantId; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
