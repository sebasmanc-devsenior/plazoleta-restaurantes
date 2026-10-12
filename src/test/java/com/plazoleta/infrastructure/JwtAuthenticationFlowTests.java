package com.plazoleta.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.plazoleta.domain.models.Role;
import com.plazoleta.domain.exceptions.MissingAuthenticatedUserException;
import com.plazoleta.domain.models.User;
import com.plazoleta.infrastructure.security.AuthenticatedUserContext;
import com.plazoleta.infrastructure.security.AuthenticatedUserPrincipal;
import com.plazoleta.infrastructure.security.JwtTokenService;

class JwtAuthenticationFlowTests {

    @Test
    void shouldResolveAuthenticatedUserIdFromSecurityContext() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(7L, "owner@test.com", Role.PROPIETARIO.name());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        Long result = AuthenticatedUserContext.resolveUserId(null);

        assertNotNull(result);
        assertEquals(7L, result);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGenerateAndParseJwtToken() {
        JwtTokenService jwtTokenService = new JwtTokenService("plazoleta-local-dev-secret-key-change-me", 3600000L);
        User user = new User(99L, "ana@test.com", "+573001234567", Role.CLIENTE, null);

        String token = jwtTokenService.generateToken(user);
        var principal = jwtTokenService.parseToken(token).orElseThrow();

        assertEquals(99L, principal.getUserId());
        assertEquals("ana@test.com", principal.getUsername());
        assertEquals(Role.CLIENTE.name(), principal.getRole());
    }

    @Test
    void employeeJwtCarriesRestaurantClaimAndRequiresIt() {
        JwtTokenService jwtTokenService = new JwtTokenService("plazoleta-local-dev-secret-key-change-me", 3600000L);
        User employee = new User(8L, "employee@test.com", "+573001234567", Role.EMPLEADO, 42L);
        var principal = jwtTokenService.parseToken(jwtTokenService.generateToken(employee)).orElseThrow();
        assertEquals(42L, principal.getRestaurantId());

        User employeeWithoutRestaurant = new User(9L, "employee2@test.com", "+573001234568", Role.EMPLEADO, null);
        assertEquals(java.util.Optional.empty(),
                jwtTokenService.parseToken(jwtTokenService.generateToken(employeeWithoutRestaurant)));
    }

    @Test
    void shouldThrowWhenNoAuthenticatedUserIsAvailable() {
        SecurityContextHolder.clearContext();

        assertThrows(MissingAuthenticatedUserException.class, () -> AuthenticatedUserContext.resolveUserId(null));
    }

    @Test
    void shouldNotTrustUserIdHeaderWithoutAuthenticatedPrincipal() {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "7");

        assertThrows(MissingAuthenticatedUserException.class,
                () -> AuthenticatedUserContext.resolveUserId(request));
    }
}
