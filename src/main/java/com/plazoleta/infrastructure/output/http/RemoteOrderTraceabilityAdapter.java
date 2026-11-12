package com.plazoleta.infrastructure.output.http;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

/**
 * HU-17: llamada HTTP al microservicio de trazabilidad: POST /internal/order-logs con el token entre servicios.
 * Si trazabilidad no responde, el cambio de estado del pedido no se deshace: se deja una advertencia en el log.
 */
@Component
public class RemoteOrderTraceabilityAdapter implements OrderTraceabilityPort {
    private static final Logger log = LoggerFactory.getLogger(RemoteOrderTraceabilityAdapter.class);

    private final RestClient client;
    private final String internalToken;

    public RemoteOrderTraceabilityAdapter(
            @Value("${app.traceability.base-url}") String baseUrl,
            @Value("${app.internal.token}") String internalToken) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.internalToken = internalToken;
    }

    @Override
    public void record(Order order, OrderStatus previousStatus) {
        // LinkedHashMap y no Map.of: employeeId y previousStatus pueden ser null.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderId", order.getId());
        body.put("clientId", order.getClientId());
        body.put("restaurantId", order.getRestaurantId());
        body.put("employeeId", order.getEmployeeId());
        body.put("previousStatus", previousStatus == null ? null : previousStatus.name());
        body.put("newStatus", order.getStatus().name());
        try {
            client.post().uri("/internal/order-logs")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Traceability log not recorded for order {} ({} -> {}): {}", order.getId(), previousStatus,
                    order.getStatus(), ex.getMessage());
        }
    }
}
