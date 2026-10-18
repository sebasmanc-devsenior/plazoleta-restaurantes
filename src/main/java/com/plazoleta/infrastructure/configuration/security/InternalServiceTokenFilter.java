package com.plazoleta.infrastructure.configuration.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica las llamadas entre microservicios a las rutas /internal/** comparando el
 * encabezado "Authorization: Bearer <token>" con el token interno compartido. Si coincide,
 * la petición recibe el rol INTERNAL. No se registra como @Component para que solo se
 * ejecute dentro de la cadena de seguridad.
 */
public class InternalServiceTokenFilter extends OncePerRequestFilter {
    public static final String INTERNAL_ROLE = "INTERNAL";
    private final byte[] expectedToken;

    public InternalServiceTokenFilter(String internalToken) {
        if (internalToken == null || internalToken.isBlank()) {
            throw new IllegalStateException("Set USERS_INTERNAL_TOKEN to protect internal routes");
        }
        this.expectedToken = internalToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            byte[] supplied = authorization.substring(7).trim().getBytes(StandardCharsets.UTF_8);
            if (MessageDigest.isEqual(expectedToken, supplied)) {
                var authentication = new UsernamePasswordAuthenticationToken("internal-service", null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + INTERNAL_ROLE)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
