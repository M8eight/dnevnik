package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;

public class BadRequestException extends BaseException {
    public BadRequestException(String message, IExceptionCode code) {
        super(message, code);
    }
}
