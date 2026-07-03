package com.rusobr.user.web.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        ExceptionCode code,
        String path
) {
}
