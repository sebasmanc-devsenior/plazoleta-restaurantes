package com.plazoleta.domain.exception;

public class RestaurantOwnershipException extends ForbiddenOperationException {
    public RestaurantOwnershipException(String message) {
        super(message);
    }
}
