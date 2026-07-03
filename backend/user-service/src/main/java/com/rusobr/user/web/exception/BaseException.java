package com.rusobr.user.web.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ExceptionCode code;

    public BaseException(String message, ExceptionCode code) {
        super(message);
        this.code = code;
    }

    public BaseException(String message, Throwable cause, ExceptionCode code) {
        super(message, cause);
        this.code = code;
    }

}
