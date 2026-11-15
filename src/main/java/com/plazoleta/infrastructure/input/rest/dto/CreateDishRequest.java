package com.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cuerpo de POST /api/dishes (HU-3). Jakarta revisa el formato; las mismas reglas siguen en el dominio (Dish).
 * Los campos van en el orden en que el dominio los valida.
 */
public record CreateDishRequest(
        @NotBlank(message = MensajesError.NOMBRE_DEL_PLATO_ES_OBLIGATORIO)
        String name,
        @NotNull(message = MensajesError.PRECIO_DEBE_SER_UN_NUMERO_ENTERO_MAYOR)
        @Positive(message = MensajesError.PRECIO_DEBE_SER_UN_NUMERO_ENTERO_MAYOR)
        Integer price,
        @NotBlank(message = MensajesError.DESCRIPCION_ES_OBLIGATORIA)
        String description,
        @NotBlank(message = MensajesError.URL_DE_LA_IMAGEN_ES_OBLIGATORIA)
        String imageUrl,
        @NotBlank(message = MensajesError.CATEGORIA_ES_OBLIGATORIA)
        String category,
        @NotNull(message = MensajesError.RESTAURANTE_ES_OBLIGATORIO)
        Long restaurantId) {
}
