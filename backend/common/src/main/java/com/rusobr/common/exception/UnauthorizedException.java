package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message, IExceptionCode code) {
        super(message, code);
    }
}
