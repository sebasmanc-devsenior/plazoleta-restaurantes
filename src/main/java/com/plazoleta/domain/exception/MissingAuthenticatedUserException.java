package com.plazoleta.domain.exception;

public class MissingAuthenticatedUserException extends RuntimeException {
    public MissingAuthenticatedUserException(String message) {
        super(message);
    }
}
