package com.rusobr.academic.web.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path
) {
}
