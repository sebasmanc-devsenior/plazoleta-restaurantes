package com.plazoleta.infrastructure.output.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.sun.net.httpserver.HttpServer;

/** HU-17: el adaptador llama de verdad al microservicio de trazabilidad (aquí, un servidor HTTP de prueba). */
class RemoteOrderTraceabilityAdapterTest {
    private HttpServer traceabilityService;
    private RemoteOrderTraceabilityAdapter adapter;
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();

    @BeforeEach
    void startTraceabilityService() throws IOException {
        traceabilityService = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        traceabilityService.start();
        adapter = new RemoteOrderTraceabilityAdapter(
                "http://127.0.0.1:" + traceabilityService.getAddress().getPort(), "test-internal-token");
    }

    @AfterEach
    void stopTraceabilityService() {
        traceabilityService.stop(0);
    }

    private void traceabilityServiceAnswers(int status) {
        traceabilityService.createContext("/", exchange -> {
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

    private static Order order(OrderStatus status, Long employeeId) {
        return new Order(5L, 7L, 1L, status, LocalDateTime.of(2026, 11, 12, 12, 0),
                List.of(new OrderItem(10L, 2)), employeeId);
    }

    @Test
    void postsTheStatusChangeWithTheInternalToken() {
        traceabilityServiceAnswers(201);

        adapter.record(order(OrderStatus.EN_PREPARACION, 9L), OrderStatus.PENDIENTE);

        assertThat(path.get()).isEqualTo("POST /internal/order-logs");
        assertThat(authorization.get()).isEqualTo("Bearer test-internal-token");
        assertThat(body.get()).contains("\"orderId\":5").contains("\"clientId\":7").contains("\"restaurantId\":1")
                .contains("\"employeeId\":9").contains("\"previousStatus\":\"PENDIENTE\"")
                .contains("\"newStatus\":\"EN_PREPARACION\"");
    }

    @Test
    void sendsANewOrderWithoutPreviousStatusNorEmployee() {
        traceabilityServiceAnswers(201);

        adapter.record(order(OrderStatus.PENDIENTE, null), null);

        assertThat(body.get()).contains("\"previousStatus\":null").contains("\"employeeId\":null")
                .contains("\"newStatus\":\"PENDIENTE\"");
    }

    @Test
    void aFailureOfTheTraceabilityServiceDoesNotStopTheOrder() {
        traceabilityServiceAnswers(500);

        assertThatCode(() -> adapter.record(order(OrderStatus.LISTO, 9L), OrderStatus.EN_PREPARACION))
                .doesNotThrowAnyException();
    }

    @Test
    void anUnreachableTraceabilityServiceDoesNotStopTheOrder() {
        traceabilityService.stop(0);

        assertThatCode(() -> adapter.record(order(OrderStatus.LISTO, 9L), OrderStatus.EN_PREPARACION))
                .doesNotThrowAnyException();
    }
}
