package com.rusobr.user.web.exception;

public class AcademicServiceUnavailableException extends BaseException {
    public AcademicServiceUnavailableException(String message, Throwable cause, ExceptionCode code) {
        super(message, cause, code);
    }
}
