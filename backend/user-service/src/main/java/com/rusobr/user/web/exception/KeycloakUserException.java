package com.rusobr.user.web.exception;

public class KeycloakUserException extends BaseException {
    public KeycloakUserException(String message, ExceptionCode code) {
        super(message, code);
    }
}
