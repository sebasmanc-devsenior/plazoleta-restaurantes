package com.plazoleta.infrastructure.output.http;

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

    public RemoteSmsNotificationAdapter(
            @Value("${app.messaging.base-url}") String baseUrl,
            @Value("${app.internal.token}") String internalToken) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
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
