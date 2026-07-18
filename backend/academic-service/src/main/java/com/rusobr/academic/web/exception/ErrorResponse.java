package com.rusobr.academic.web.exception;

import com.rusobr.common.enums.IExceptionCode;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        IExceptionCode code,
        String path
) {
}
