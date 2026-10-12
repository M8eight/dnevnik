package com.rusobr.mail.web.dto;

import java.time.Instant;

public record MailResponse(
        Long id,
        Long senderId,
        String subject,
        String body,
        Instant sentAt
) {
}
