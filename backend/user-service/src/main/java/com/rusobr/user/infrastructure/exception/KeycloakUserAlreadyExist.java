package com.rusobr.user.infrastructure.exception;

public class KeycloakUserAlreadyExist extends RuntimeException {
    public KeycloakUserAlreadyExist(String message) {
        super(message);
    }
}
