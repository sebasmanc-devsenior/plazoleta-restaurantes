package com.plazoleta.infrastructure.output.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.plazoleta.domain.exception.IntegrationUnavailableException;
import com.plazoleta.infrastructure.output.pin.SecureRandomPinGenerator;
import com.sun.net.httpserver.HttpServer;

/** HU-14: el adaptador llama de verdad al microservicio de mensajería (aquí, un servidor HTTP de prueba). */
class RemoteSmsNotificationAdapterTest {
    private HttpServer messagingService;
    private RemoteSmsNotificationAdapter adapter;
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();

    @BeforeEach
    void startMessagingService() throws IOException {
        messagingService = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        messagingService.start();
        adapter = new RemoteSmsNotificationAdapter("http://127.0.0.1:" + messagingService.getAddress().getPort(),
                "test-internal-token");
    }

    @AfterEach
    void stopMessagingService() {
        messagingService.stop(0);
    }

    private void messagingServiceAnswers(int status) {
        messagingService.createContext("/", exchange -> {
            path.set(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
    }

    @Test
    void postsThePhoneAndTheMessageWithTheInternalToken() {
        messagingServiceAnswers(200);

        adapter.send("+573001112233", "Tu pedido #5 está listo. PIN: 048213");

        assertThat(path.get()).isEqualTo("POST /internal/sms");
        assertThat(authorization.get()).isEqualTo("Bearer test-internal-token");
        assertThat(body.get()).contains("\"phone\":\"+573001112233\"").contains("048213");
    }

    @Test
    void reportsTheServiceAsUnavailableWhenItRejectsTheSms() {
        messagingServiceAnswers(502);

        assertThatThrownBy(() -> adapter.send("+573001112233", "hola"))
                .isInstanceOf(IntegrationUnavailableException.class).hasMessageContaining("502");
    }

    @Test
    void reportsTheServiceAsUnavailableWhenItDoesNotAnswer() {
        messagingService.stop(0);

        assertThatThrownBy(() -> adapter.send("+573001112233", "hola"))
                .isInstanceOf(IntegrationUnavailableException.class);
    }

    @Test
    void thePinHasSixDigits() {
        SecureRandomPinGenerator generator = new SecureRandomPinGenerator();

        for (int i = 0; i < 200; i++) {
            assertThat(generator.generate()).matches("\\d{6}");
        }
    }
}
