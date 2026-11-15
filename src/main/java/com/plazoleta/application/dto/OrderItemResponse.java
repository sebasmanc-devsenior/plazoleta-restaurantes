package com.plazoleta.application.dto;

/**
 * Un plato dentro de la respuesta de un pedido: qué plato y cuántas unidades. Es solo de salida; lo que el cliente
 * envía al crear el pedido es {@link OrderItemCommand}.
 */
public class OrderItemResponse {
    private final Long dishId;
    private final Integer quantity;

    public OrderItemResponse(Long dishId, Integer quantity) {
        this.dishId = dishId;
        this.quantity = quantity;
    }

    public Long getDishId() { return dishId; }
    public Integer getQuantity() { return quantity; }
}
