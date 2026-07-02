package com.rusobr.academic.web.exception;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
