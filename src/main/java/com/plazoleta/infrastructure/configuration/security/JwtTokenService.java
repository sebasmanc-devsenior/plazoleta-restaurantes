package com.plazoleta.infrastructure.configuration.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.plazoleta.domain.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {
    private static final Set<String> SUPPORTED_ROLES = Set.of("ADMIN", "PROPIETARIO", "EMPLEADO", "CLIENTE");
    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("restaurantId", user.getRestaurantId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    public Optional<AuthenticatedUserPrincipal> parseToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        String token = rawToken.startsWith("Bearer ") ? rawToken.substring(7).trim() : rawToken.trim();
        if (token.isBlank()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object rawUserId = claims.get("userId");
            Long userId = rawUserId == null ? Long.valueOf(claims.getSubject())
                    : Long.valueOf(rawUserId.toString());
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            Object rawRestaurantId = claims.get("restaurantId");
            Long restaurantId = rawRestaurantId == null ? null : Long.valueOf(rawRestaurantId.toString());
            if (userId <= 0 || role == null || !SUPPORTED_ROLES.contains(role)
                    || ("EMPLEADO".equals(role) && (restaurantId == null || restaurantId <= 0))) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUserPrincipal(userId, email, role, restaurantId));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
