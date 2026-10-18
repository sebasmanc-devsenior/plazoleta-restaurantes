package com.plazoleta.domain.exception;

/**
 * Un microservicio del que depende la operación no respondió o rechazó la solicitud (HTTP 503).
 */
public class IntegrationUnavailableException extends RuntimeException {
    public IntegrationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
