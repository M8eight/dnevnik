package com.rusobr.mail.web.exception;

import com.rusobr.common.exception.BaseException;

public class UserServiceUnavailableException extends BaseException {
    public UserServiceUnavailableException(String message, Throwable cause, MailExceptionCode code) {
        super(message, cause, code);
    }
}
