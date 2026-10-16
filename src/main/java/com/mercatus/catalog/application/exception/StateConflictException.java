package com.mercatus.catalog.application.exception;

public class StateConflictException extends RuntimeException {

    public StateConflictException(String message) {
        super(message);
    }
}
