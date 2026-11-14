package com.plazoleta.infrastructure.output.http;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.plazoleta.application.port.output.SmsNotificationPort;
import com.plazoleta.domain.exception.IntegrationUnavailableException;

/** HU-14: llamada HTTP al microservicio de mensajería: POST /internal/sms con el token entre servicios. */
@Component
public class RemoteSmsNotificationAdapter implements SmsNotificationPort {
    private final RestClient client;
    private final String internalToken;

    /** Constructor para las pruebas: usa los tiempos de espera por defecto. */
    public RemoteSmsNotificationAdapter(String baseUrl, String internalToken) {
        this(baseUrl, internalToken, HttpClients.DEFAULT_CONNECT_TIMEOUT, HttpClients.DEFAULT_READ_TIMEOUT);
    }

    @Autowired
    public RemoteSmsNotificationAdapter(@Value("${app.messaging.base-url}") String baseUrl, @Value("${app.internal.token}") String internalToken,
            @Value("${app.http.connect-timeout:2s}") Duration connectTimeout,
            @Value("${app.http.read-timeout:5s}") Duration readTimeout) {
        this.client = HttpClients.restClient(baseUrl, connectTimeout, readTimeout);
        this.internalToken = internalToken;
    }

    @Override
    public void send(String phone, String message) {
        try {
            client.post().uri("/internal/sms")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("phone", phone, "message", message))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new IntegrationUnavailableException("Messaging service rejected the SMS (HTTP "
                    + ex.getStatusCode().value() + ")", ex);
        } catch (RestClientException ex) {
            throw new IntegrationUnavailableException("Messaging service is unavailable", ex);
        }
    }
}
