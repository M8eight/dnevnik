package com.rusobr.academic.web.exception;

import com.rusobr.common.exception.BaseException;

public class BadRequestException extends BaseException {
    public BadRequestException(String message, AcademicExceptionCode code) {
        super(message, code);
    }
}
