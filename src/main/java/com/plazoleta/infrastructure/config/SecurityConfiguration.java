package com.plazoleta.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.plazoleta.infrastructure.security.InternalServiceTokenFilter;
import com.plazoleta.infrastructure.security.JwtAuthenticationFilter;

/**
 * Seguridad: valida el JWT emitido por users-service y aplica las reglas de rol de cada
 * endpoint (HU-5). Todo lo que no esté declarado aquí se rechaza.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   @Value("${app.internal.token}") String internalToken) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // HU-2: solo el administrador crea restaurantes.
                .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("ADMIN")
                // HU-3: solo un propietario crea platos (el dueño se valida en el caso de uso).
                .requestMatchers(HttpMethod.POST, "/api/dishes").hasRole("PROPIETARIO")
                // HU-4: solo un propietario modifica platos.
                .requestMatchers(HttpMethod.PUT, "/api/dishes/*").hasRole("PROPIETARIO")
                // HU-6: solo users-service, con el token interno, consulta el dueño de un restaurante.
                .requestMatchers(HttpMethod.GET, "/internal/restaurants/*").hasRole(InternalServiceTokenFilter.INTERNAL_ROLE)
                // Cualquier ruta no declarada arriba se rechaza: cerrado por defecto.
                .anyRequest().denyAll())
            .exceptionHandling(errors -> errors
                // Sin token o con token inválido: 401. Con token pero sin permisos: 403.
                .authenticationEntryPoint((request, response, ex) ->
                        writeError(request, response, HttpStatus.UNAUTHORIZED, "Authentication is required"))
                .accessDeniedHandler((request, response, ex) ->
                        writeError(request, response, HttpStatus.FORBIDDEN, "Insufficient permissions")))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new InternalServiceTokenFilter(internalToken), JwtAuthenticationFilter.class);
        return http.build();
    }

    private static void writeError(HttpServletRequest request, HttpServletResponse response,
                                   HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\""));
        response.getWriter().write(body);
    }
}
