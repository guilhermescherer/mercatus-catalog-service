package com.mercatus.catalog.domain.exception;

public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String message) {
        super(message);
    }
}
