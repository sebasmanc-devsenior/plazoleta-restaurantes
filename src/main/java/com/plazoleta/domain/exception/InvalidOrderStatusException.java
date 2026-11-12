package com.plazoleta.domain.exception;

/** El pedido no está en el estado que exige la operación (HU-13 a HU-16). */
public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
