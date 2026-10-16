package com.mercatus.catalog.domain.exception;

public class InvalidStateTransitionException extends DomainRuleException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
