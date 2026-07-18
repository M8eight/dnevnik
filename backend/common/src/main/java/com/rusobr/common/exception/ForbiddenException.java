package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;

public class ForbiddenException extends BaseException
{
    public ForbiddenException(String message, IExceptionCode code) {
        super(message, code);
    }
}
