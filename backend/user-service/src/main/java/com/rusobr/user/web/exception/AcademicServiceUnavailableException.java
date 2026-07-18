package com.rusobr.user.web.exception;

import com.rusobr.common.exception.BaseException;

public class AcademicServiceUnavailableException extends BaseException {
    public AcademicServiceUnavailableException(String message, Throwable cause, UserExceptionCode code) {
        super(message, cause, code);
    }
}
