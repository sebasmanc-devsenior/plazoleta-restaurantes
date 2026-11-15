package com.plazoleta.domain.model;

import com.plazoleta.domain.util.MensajesError;

/** Un plato del pedido y cuántas unidades se piden (HU-11). */
public class OrderItem {
    private final Long dishId;
    private final int quantity;

    public OrderItem(Long dishId, Integer quantity) {
        if (dishId == null) {
            throw new IllegalArgumentException(MensajesError.CADA_PLATO_DEL_PEDIDO_NECESITA_UN_DISHID);
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(MensajesError.CANTIDAD_DEBE_SER_UN_NUMERO_ENTERO_MAYOR);
        }
        this.dishId = dishId;
        this.quantity = quantity;
    }

    public Long getDishId() { return dishId; }
    public int getQuantity() { return quantity; }
}
