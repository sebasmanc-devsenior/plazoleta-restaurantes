package com.plazoleta.domain.exceptions;

public class MissingAuthenticatedUserException extends RuntimeException {
    public MissingAuthenticatedUserException(String message) {
        super(message);
    }
}
