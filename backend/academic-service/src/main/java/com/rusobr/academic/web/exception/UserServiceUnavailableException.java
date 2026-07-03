package com.rusobr.academic.web.exception;

public class UserServiceUnavailableException extends BaseException {
    public UserServiceUnavailableException(String message, Throwable cause, ExceptionCode code) {
        super(message, cause, code);
    }
}
