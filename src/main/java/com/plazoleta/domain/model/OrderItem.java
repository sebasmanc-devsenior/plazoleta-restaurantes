package com.plazoleta.domain.model;

/** Un plato del pedido y cuántas unidades se piden (HU-11). */
public class OrderItem {
    private final Long dishId;
    private final int quantity;

    public OrderItem(Long dishId, Integer quantity) {
        if (dishId == null) {
            throw new IllegalArgumentException("Each order item needs a dishId");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be an integer greater than 0");
        }
        this.dishId = dishId;
        this.quantity = quantity;
    }

    public Long getDishId() { return dishId; }
    public int getQuantity() { return quantity; }
}
