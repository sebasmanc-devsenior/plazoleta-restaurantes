package com.plazoleta.domain.model;

import com.plazoleta.domain.util.MensajesError;

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
            MensajesError.PEDIDO_NO_CANCELABLE;

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
            throw new IllegalArgumentException(MensajesError.CLIENTE_ES_OBLIGATORIO);
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException(MensajesError.RESTAURANTE_ES_OBLIGATORIO);
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(MensajesError.PEDIDO_DEBE_TENER_AL_MENOS_UN_PLATO);
        }
        Set<Long> dishIds = new HashSet<>();
        for (OrderItem item : items) {
            if (!dishIds.add(item.getDishId())) {
                throw new IllegalArgumentException(MensajesError.CADA_PLATO_PUEDE_APARECER_UNA_SOLA_VEZ);
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
            throw new IllegalArgumentException(MensajesError.EMPLEADO_ES_OBLIGATORIO);
        }
        if (status != OrderStatus.PENDIENTE) {
            throw new InvalidOrderStatusException(MensajesError.SOLO_SE_PUEDEN_ASIGNAR_PEDIDOS_PENDIENTE_ESTE + status);
        }
        this.employeeId = employeeId;
        this.status = OrderStatus.EN_PREPARACION;
    }

    /** HU-14: el pedido pasa de EN_PREPARACION a LISTO y guarda el PIN con el que el cliente lo reclama. */
    public void markReady(String securityPin) {
        if (securityPin == null || securityPin.isBlank()) {
            throw new IllegalArgumentException(MensajesError.PIN_DE_SEGURIDAD_ES_OBLIGATORIO);
        }
        if (status != OrderStatus.EN_PREPARACION) {
            throw new InvalidOrderStatusException(MensajesError.SOLO_SE_PUEDEN_MARCAR_COMO_LISTOS_LOS
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
            throw new InvalidOrderStatusException(MensajesError.SOLO_SE_PUEDEN_ENTREGAR_PEDIDOS_LISTO_ESTE + status);
        }
        if (pin == null || pin.isBlank()) {
            throw new IllegalArgumentException(MensajesError.PIN_DE_SEGURIDAD_ES_OBLIGATORIO);
        }
        if (!pin.trim().equals(securityPin)) {
            throw new InvalidSecurityPinException(MensajesError.PIN_DE_SEGURIDAD_NO_ES_CORRECTO);
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
            throw new InvalidOrderStatusException(MensajesError.SOLO_UN_PEDIDO_LISTO_PUEDE_VOLVER_A
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
