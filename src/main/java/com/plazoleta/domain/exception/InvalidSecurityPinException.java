package com.plazoleta.domain.exception;

/** HU-15: el PIN digitado no es el que se le envió al cliente (HTTP 400). */
public class InvalidSecurityPinException extends RuntimeException {
    public InvalidSecurityPinException(String message) {
        super(message);
    }
}
