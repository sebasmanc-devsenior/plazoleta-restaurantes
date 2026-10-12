package com.plazoleta.infrastructure.adapters.output.http;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.domain.models.Role;
import com.plazoleta.domain.exceptions.IntegrationUnavailableException;
import com.plazoleta.domain.models.User;
import com.plazoleta.infrastructure.security.AuthenticatedUserContext;
import com.plazoleta.infrastructure.security.AuthenticatedUserPrincipal;

@Repository
public class RemoteUserRepository implements UserRepositoryPort {
    private final RestClient client;
    private final String internalToken;

    public RemoteUserRepository(
            @Value("${app.users.base-url}") String baseUrl,
            @Value("${app.internal.token}") String internalToken) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        AuthenticatedUserPrincipal principal = AuthenticatedUserContext.currentPrincipal();
        if (principal != null && principal.getUserId().equals(id)) {
            User claimUser = new User();
            claimUser.setId(id);
            claimUser.setEmail(principal.getEmail());
            claimUser.setRole(Role.valueOf(principal.getRole()));
            claimUser.setRestaurantId(principal.getRestaurantId());
            return Optional.of(claimUser);
        }
        try {
            UserDirectoryResponse response = client.get().uri("/internal/users/{id}", id)
                    .header(org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer " + internalToken)
                    .retrieve().body(UserDirectoryResponse.class);
            return Optional.ofNullable(response).map(UserDirectoryResponse::toDomain);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new IntegrationUnavailableException("Users service rejected the lookup (HTTP "
                    + ex.getStatusCode().value() + ")", ex);
        } catch (RestClientException ex) {
            throw new IntegrationUnavailableException("Users service is unavailable", ex);
        }
    }

    /** Contrato JSON de GET /internal/users/{id} en users-service. Se mapea al dominio aquí. */
    private record UserDirectoryResponse(Long id, String role, String phone, Long restaurantId) {
        User toDomain() {
            return new User(id, null, phone, role == null ? null : Role.valueOf(role), restaurantId);
        }
    }
}
