package com.plazoleta.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Documentación OpenAPI. Todos los endpoints exigen el JWT emitido por users-service
 * (POST /api/login en el puerto 8081); en Swagger se pega con el botón "Authorize".
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Plazoleta API", version = "1.0",
                description = "Restaurants and dishes (HU 2, 3, 4, 7, 9). Get the token from users-service (POST /api/login)."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfiguration {
}
