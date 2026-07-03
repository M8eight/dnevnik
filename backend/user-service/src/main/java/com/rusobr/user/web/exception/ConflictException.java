package com.rusobr.user.web.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message, ExceptionCode code) {
        super(message, code);
    }
}
