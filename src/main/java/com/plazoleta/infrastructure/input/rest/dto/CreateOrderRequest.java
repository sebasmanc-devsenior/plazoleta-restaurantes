package com.plazoleta.infrastructure.input.rest.dto;

import java.util.List;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de POST /api/orders (HU-11): el restaurante y los platos con su cantidad. El cliente sale del token.
 * Las reglas del pedido (platos del mismo restaurante, disponibles, sin repetir) las revisa el caso de uso.
 */
public record CreateOrderRequest(
        @NotNull(message = MensajesError.RESTAURANTE_ES_OBLIGATORIO)
        Long restaurantId,
        @NotEmpty(message = MensajesError.PEDIDO_DEBE_TENER_AL_MENOS_UN_PLATO)
        @Valid
        List<OrderItemRequest> dishes) {
}
