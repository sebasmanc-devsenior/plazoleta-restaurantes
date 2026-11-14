package com.plazoleta.infrastructure.output.http;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.IntegrationUnavailableException;
import com.plazoleta.domain.model.User;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserContext;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserPrincipal;

@Repository
public class RemoteUserRepository implements UserRepositoryPort {
    private final RestClient client;
    private final String internalToken;

    /** Constructor para las pruebas: usa los tiempos de espera por defecto. */
    public RemoteUserRepository(String baseUrl, String internalToken) {
        this(baseUrl, internalToken, HttpClients.DEFAULT_CONNECT_TIMEOUT, HttpClients.DEFAULT_READ_TIMEOUT);
    }

    @Autowired
    public RemoteUserRepository(@Value("${app.users.base-url}") String baseUrl, @Value("${app.internal.token}") String internalToken,
            @Value("${app.http.connect-timeout:2s}") Duration connectTimeout,
            @Value("${app.http.read-timeout:5s}") Duration readTimeout) {
        this.client = HttpClients.restClient(baseUrl, connectTimeout, readTimeout);
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
        return findRegisteredById(id);
    }

    /** Llamada HTTP a users-service: GET /internal/users/{id} con el token entre servicios. */
    @Override
    public Optional<User> findRegisteredById(Long id) {
        if (id == null) {
            return Optional.empty();
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
