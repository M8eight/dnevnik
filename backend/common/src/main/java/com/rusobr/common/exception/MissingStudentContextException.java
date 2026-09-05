package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;

public class MissingStudentContextException extends BaseException {
    public MissingStudentContextException(String message, IExceptionCode code) {
        super(message, code);
    }
}
