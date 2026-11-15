package com.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Un plato del pedido (HU-11) y cuántas unidades se piden. */
public record OrderItemRequest(
        @NotNull(message = MensajesError.CADA_PLATO_DEL_PEDIDO_NECESITA_UN_DISHID)
        Long dishId,
        @NotNull(message = MensajesError.CANTIDAD_DEBE_SER_UN_NUMERO_ENTERO_MAYOR)
        @Positive(message = MensajesError.CANTIDAD_DEBE_SER_UN_NUMERO_ENTERO_MAYOR)
        Integer quantity) {
}
