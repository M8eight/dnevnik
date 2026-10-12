package com.rusobr.mail;

import java.time.Instant;
import java.util.List;

public record MailSentEvent(
        Long mailId,
        Long senderId,
        List<Long> recipientId,
        String subject,
        Instant sentAt
) {
}
