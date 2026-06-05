package com.rusobr.user.web.exception;

public class KeycloakUserAlreadyExist extends RuntimeException {
    public KeycloakUserAlreadyExist(String message) {
        super(message);
    }
}
