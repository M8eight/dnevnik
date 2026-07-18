package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;

public class NotFoundException extends BaseException {
    public NotFoundException(String message, IExceptionCode code) {
        super(message, code);
    }
}
