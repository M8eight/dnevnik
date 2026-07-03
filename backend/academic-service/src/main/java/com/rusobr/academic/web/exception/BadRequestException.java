package com.rusobr.academic.web.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(String message, ExceptionCode code) {
        super(message, code);
    }
}
