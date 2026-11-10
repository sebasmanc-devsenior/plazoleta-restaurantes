package com.plazoleta.domain.exception;

/** HU-11: el cliente ya tiene un pedido pendiente, en preparación o listo. */
public class ActiveOrderExistsException extends RuntimeException {
    public ActiveOrderExistsException(String message) {
        super(message);
    }
}
