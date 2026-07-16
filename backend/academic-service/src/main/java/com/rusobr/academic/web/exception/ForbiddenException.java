package com.rusobr.academic.web.exception;

public class ForbiddenException extends BaseException
{
    public ForbiddenException(String message, ExceptionCode code) {
        super(message, code);
    }
}
