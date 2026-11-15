package com.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.domain.util.MensajesError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Cuerpo de POST /api/restaurants (HU-2). Jakarta revisa el formato antes de llegar al caso de uso; las mismas
 * reglas siguen en el dominio (Restaurant). Los campos van en el orden en que el dominio los valida: si hay
 * varios errores, el mensaje principal es el del primero.
 */
public record CreateRestaurantRequest(
        @NotBlank(message = MensajesError.NOMBRE_DEL_RESTAURANTE_ES_OBLIGATORIO)
        @Pattern(regexp = "(?s)(?!\\s*\\d+\\s*$).*", message = MensajesError.NOMBRE_DEL_RESTAURANTE_NO_PUEDE_TENER_SOLO)
        String name,
        @NotNull(message = MensajesError.NIT_SOLO_PUEDE_TENER_DIGITOS)
        @Pattern(regexp = "\\d+", message = MensajesError.NIT_SOLO_PUEDE_TENER_DIGITOS)
        String nit,
        @NotBlank(message = MensajesError.DIRECCION_ES_OBLIGATORIA)
        String address,
        @NotNull(message = MensajesError.TELEFONO_DEL_RESTAURANTE_DEBE_TENER_MAXIMO_13)
        @Pattern(regexp = "\\+\\d{7,12}|\\d{7,13}", message = MensajesError.TELEFONO_DEL_RESTAURANTE_DEBE_TENER_MAXIMO_13)
        String phone,
        @NotBlank(message = MensajesError.URL_DEL_LOGO_ES_OBLIGATORIA)
        String logoUrl,
        @NotNull(message = MensajesError.PROPIETARIO_DEL_RESTAURANTE_ES_OBLIGATORIO)
        Long ownerUserId) {
}
