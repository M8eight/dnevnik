package com.rusobr.user.web.exception;

public class ForbiddenException extends BaseException
{
    public ForbiddenException(String message, ExceptionCode code) {
        super(message, code);
    }
}
