package com.mercatus.catalog.domain.exception;

public class DuplicateSkuCodeException extends RuntimeException {

    public DuplicateSkuCodeException(String message) {
        super(message);
    }
}
