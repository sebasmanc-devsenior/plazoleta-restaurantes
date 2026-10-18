package com.plazoleta.infrastructure.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import com.plazoleta.domain.exception.MissingAuthenticatedUserException;

public final class AuthenticatedUserContext {
    private AuthenticatedUserContext() {
    }

    public static Long resolveUserId(HttpServletRequest request) {
        Long userId = resolveUserIdFromSecurityContext();
        if (userId == null) {
            throw new MissingAuthenticatedUserException("No authenticated user available");
        }
        return userId;
    }

    public static Long resolveUserIdFromSecurityContext() {
        AuthenticatedUserPrincipal principal = currentPrincipal();
        return principal == null ? null : principal.getUserId();
    }

    public static AuthenticatedUserPrincipal currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static void requireRole(String role) {
        AuthenticatedUserPrincipal principal = currentPrincipal();
        if (principal == null || !role.equals(principal.getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("Insufficient role");
        }
    }
}
