package com.plazoleta.domain.exception;

/**
 * El usuario autenticado no tiene permiso para operar sobre el recurso indicado (HTTP 403).
 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
