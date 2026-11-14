package com.plazoleta.infrastructure.output.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.plazoleta.domain.exception.IntegrationUnavailableException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;

/** Las llamadas a otros microservicios no se quedan esperando si el otro servicio se cuelga. */
class HttpTimeoutsTest {
    /** Servidor que acepta la conexión pero nunca responde: así se ve un servicio colgado. */
    private ServerSocket silentServer;

    @BeforeEach
    void openSilentServer() throws IOException {
        silentServer = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
    }

    @AfterEach
    void closeSilentServer() throws IOException {
        silentServer.close();
    }

    private String silentUrl() {
        return "http://127.0.0.1:" + silentServer.getLocalPort();
    }

    private static final Duration SHORT = Duration.ofMillis(300);

    @Test
    void theUsersServiceHangingAnswersUnavailableInTime() {
        RemoteUserRepository users = new RemoteUserRepository(silentUrl(), "token", SHORT, SHORT);
        long start = System.nanoTime();

        assertThatThrownBy(() -> users.findRegisteredById(7L)).isInstanceOf(IntegrationUnavailableException.class);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(3));
    }

    @Test
    void theMessagingServiceHangingAnswersUnavailableInTime() {
        RemoteSmsNotificationAdapter sms = new RemoteSmsNotificationAdapter(silentUrl(), "token", SHORT, SHORT);
        long start = System.nanoTime();

        assertThatThrownBy(() -> sms.send("+573001112233", "hola")).isInstanceOf(IntegrationUnavailableException.class);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(3));
    }

    @Test
    void theTraceabilityServiceHangingDoesNotHoldTheOrder() {
        RemoteOrderTraceabilityAdapter traceability = new RemoteOrderTraceabilityAdapter(silentUrl(), "token", SHORT, SHORT);
        Order order = new Order(5L, 7L, 1L, OrderStatus.PENDIENTE, java.time.LocalDateTime.of(2026, 11, 14, 12, 0),
                java.util.List.of(new OrderItem(10L, 1)));
        long start = System.nanoTime();

        assertThatCode(() -> traceability.record(order, null)).doesNotThrowAnyException();
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(3));
    }

    @Test
    void theDefaultsAreTwoSecondsToConnectAndFiveToAnswer() {
        assertThat(HttpClients.DEFAULT_CONNECT_TIMEOUT).isEqualTo(Duration.ofSeconds(2));
        assertThat(HttpClients.DEFAULT_READ_TIMEOUT).isEqualTo(Duration.ofSeconds(5));
    }
}
