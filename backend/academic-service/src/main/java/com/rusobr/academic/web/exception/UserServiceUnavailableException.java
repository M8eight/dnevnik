package com.rusobr.academic.web.exception;

import com.rusobr.common.exception.BaseException;

public class UserServiceUnavailableException extends BaseException {
    public UserServiceUnavailableException(String message, Throwable cause, AcademicExceptionCode code) {
        super(message, cause, code);
    }
}
