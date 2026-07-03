package com.rusobr.user.web.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String message, ExceptionCode code) {
        super(message, code);
    }
}
