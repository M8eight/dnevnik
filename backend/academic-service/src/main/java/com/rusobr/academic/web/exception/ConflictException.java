package com.rusobr.academic.web.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message, ExceptionCode code) {
        super(message, code);
    }
}
