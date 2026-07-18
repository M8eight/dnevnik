package com.rusobr.user.web.exception;

import com.rusobr.common.exception.BaseException;

public class KeycloakUserException extends BaseException {
    public KeycloakUserException(String message, UserExceptionCode code) {
        super(message, code);
    }
}
