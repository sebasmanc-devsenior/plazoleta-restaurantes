package com.plazoleta.domain.exceptions;

public class RestaurantOwnershipException extends ForbiddenOperationException {
    public RestaurantOwnershipException(String message) {
        super(message);
    }
}
