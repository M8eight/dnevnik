package com.rusobr.academic.web.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message, ExceptionCode code) {
        super(message, code);
    }
}
