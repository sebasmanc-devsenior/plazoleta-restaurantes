package com.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de PATCH /api/dishes/{dishId}/availability (HU-7): true habilita el plato, false lo deshabilita. */
public record DishAvailabilityRequest(
        @NotNull(message = MensajesError.DISPONIBILIDAD_ES_OBLIGATORIA)
        Boolean active) {
}
