package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;

public class ConflictException extends BaseException {
    public ConflictException(String message, IExceptionCode code) {
        super(message, code);
    }
}
