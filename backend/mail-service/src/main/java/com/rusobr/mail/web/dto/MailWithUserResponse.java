package com.rusobr.mail.web.dto;

import com.rusobr.common.dto.UserFeignResponse;

import java.time.Instant;

public record MailWithUserResponse(
        Long mailId,
        UserFeignResponse sender,
        String subject,
        String body,
        Instant sentAt
) {
}
