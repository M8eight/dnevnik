package com.rusobr.user.infrastructure.exception;

public class KeycloackUserAlreadyExist extends RuntimeException {
    public KeycloackUserAlreadyExist(String message) {
        super(message);
    }
}
