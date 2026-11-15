package com.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Cuerpo de PUT /api/dishes/{dishId} (HU-4): solo cambian el precio y la descripción. */
public record UpdateDishRequest(
        @NotNull(message = MensajesError.PRECIO_DEBE_SER_MAYOR_QUE_0)
        @Positive(message = MensajesError.PRECIO_DEBE_SER_MAYOR_QUE_0)
        Integer price,
        @NotBlank(message = MensajesError.DESCRIPCION_ES_OBLIGATORIA)
        String description) {
}
