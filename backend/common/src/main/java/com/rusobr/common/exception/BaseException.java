package com.rusobr.common.exception;

import com.rusobr.common.enums.IExceptionCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final IExceptionCode code;

    public BaseException(String message, IExceptionCode code) {
        super(message);
        this.code = code;
    }

    public BaseException(String message, Throwable cause, IExceptionCode code) {
        super(message, cause);
        this.code = code;
    }

}
