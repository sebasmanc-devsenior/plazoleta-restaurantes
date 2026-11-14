package com.plazoleta.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.InvalidSecurityPinException;

/** Pedido de un cliente a un restaurante. */
public class Order {
    /** HU-16: mensaje exigido por el criterio de aceptación cuando el pedido ya no está PENDIENTE. */
    public static final String CANNOT_CANCEL_MESSAGE =
            "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";

    private final Long id;
    private final Long clientId;
    private final Long restaurantId;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;
    private Long employeeId;
    private String securityPin;

    public Order(Long id, Long clientId, Long restaurantId, OrderStatus status, LocalDateTime createdAt,
                 List<OrderItem> items) {
        this(id, clientId, restaurantId, status, createdAt, items, null);
    }

    public Order(Long id, Long clientId, Long restaurantId, OrderStatus status, LocalDateTime createdAt,
                 List<OrderItem> items, Long employeeId) {
        this(id, clientId, restaurantId, status, createdAt, items, employeeId, null);
    }

    public Order(Long id, Long clientId, Long restaurantId, OrderStatus status, LocalDateTime createdAt,
                 List<OrderItem> items, Long employeeId, String securityPin) {
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
        this.securityPin = securityPin;
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

    /** HU-14: el pedido pasa de EN_PREPARACION a LISTO y guarda el PIN con el que el cliente lo reclama. */
    public void markReady(String securityPin) {
        if (securityPin == null || securityPin.isBlank()) {
            throw new IllegalArgumentException("Security PIN is required");
        }
        if (status != OrderStatus.EN_PREPARACION) {
            throw new InvalidOrderStatusException("Only EN_PREPARACION orders can be marked as ready; this order is "
                    + status);
        }
        this.securityPin = securityPin;
        this.status = OrderStatus.LISTO;
    }

    /**
     * HU-15: el pedido pasa de LISTO a ENTREGADO solo si el PIN digitado es el que recibió el cliente.
     * ENTREGADO es un estado final: ningún método de esta clase lo acepta como punto de partida.
     */
    public void deliver(String pin) {
        if (status != OrderStatus.LISTO) {
            throw new InvalidOrderStatusException("Only LISTO orders can be delivered; this order is " + status);
        }
        if (pin == null || pin.isBlank()) {
            throw new IllegalArgumentException("Security PIN is required");
        }
        if (!pin.trim().equals(securityPin)) {
            throw new InvalidSecurityPinException("The security PIN is not correct");
        }
        this.status = OrderStatus.ENTREGADO;
    }

    /** HU-16: solo un pedido PENDIENTE se puede cancelar. */
    public void cancel() {
        if (status != OrderStatus.PENDIENTE) {
            throw new InvalidOrderStatusException(CANNOT_CANCEL_MESSAGE);
        }
        this.status = OrderStatus.CANCELADO;
    }

    /**
     * HU-14: si el SMS con el PIN no se pudo enviar, el pedido vuelve a EN_PREPARACION y se olvida el PIN,
     * para que el empleado pueda reintentar y el cliente nunca tenga un PIN que no recibió.
     */
    public void undoReady() {
        if (status != OrderStatus.LISTO) {
            throw new InvalidOrderStatusException("Only LISTO orders can go back to EN_PREPARACION; this order is "
                    + status);
        }
        this.securityPin = null;
        this.status = OrderStatus.EN_PREPARACION;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Long getRestaurantId() { return restaurantId; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
    public Long getEmployeeId() { return employeeId; }
    public String getSecurityPin() { return securityPin; }
}
