package com.plazoleta.infrastructure.output.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.plazoleta.domain.exception.IntegrationUnavailableException;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.model.User;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserPrincipal;
import com.sun.net.httpserver.HttpServer;

/** HU-11: el adaptador llama de verdad al microservicio de usuarios (aquí, un servidor HTTP de prueba). */
class RemoteUserRepositoryTest {
    private HttpServer usersService;
    private RemoteUserRepository repository;
    private final AtomicReference<String> requestedPath = new AtomicReference<>();
    private final AtomicReference<String> receivedAuthorization = new AtomicReference<>();

    @BeforeEach
    void startUsersService() throws IOException {
        usersService = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        usersService.start();
        repository = new RemoteUserRepository("http://127.0.0.1:" + usersService.getAddress().getPort(),
                "test-internal-token");
    }

    @AfterEach
    void stopUsersService() {
        usersService.stop(0);
        SecurityContextHolder.clearContext();
    }

    private void usersServiceAnswers(int status, String json) {
        usersService.createContext("/internal/users/", exchange -> {
            requestedPath.set(exchange.getRequestURI().getPath());
            receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }

    private static void authenticatedAs(long userId, String role) {
        var principal = new AuthenticatedUserPrincipal(userId, "cliente@mail.com", role, null);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    void asksTheUsersServiceEvenWhenTheUserIsTheOneInTheToken() {
        authenticatedAs(7L, "CLIENTE");
        usersServiceAnswers(200, "{\"id\":7,\"role\":\"CLIENTE\",\"phone\":\"+573001112233\",\"restaurantId\":null}");

        User client = repository.findRegisteredById(7L).orElseThrow();

        assertThat(requestedPath.get()).isEqualTo("/internal/users/7");
        assertThat(receivedAuthorization.get()).isEqualTo("Bearer test-internal-token");
        assertThat(client.getId()).isEqualTo(7L);
        assertThat(client.getRole()).isEqualTo(Role.CLIENTE);
        assertThat(client.getPhone()).isEqualTo("+573001112233");
    }

    @Test
    void returnsEmptyWhenTheUsersServiceDoesNotKnowTheUser() {
        usersServiceAnswers(404, "{\"message\":\"User was not found\"}");

        Optional<User> client = repository.findRegisteredById(99L);

        assertThat(client).isEmpty();
    }

    @Test
    void reportsTheUsersServiceAsUnavailableWhenItDoesNotAnswer() {
        usersService.stop(0);

        assertThatThrownBy(() -> repository.findRegisteredById(7L))
                .isInstanceOf(IntegrationUnavailableException.class);
    }
}
