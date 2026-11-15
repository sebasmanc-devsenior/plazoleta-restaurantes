package com.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de PATCH /api/orders/{orderId}/deliver (HU-15): el PIN que el cliente le dicta al empleado. */
public record DeliverOrderRequest(
        @NotBlank(message = MensajesError.PIN_DE_SEGURIDAD_ES_OBLIGATORIO)
        String pin) {
}
