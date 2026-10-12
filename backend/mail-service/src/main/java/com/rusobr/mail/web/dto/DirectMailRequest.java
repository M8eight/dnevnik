package com.rusobr.mail.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DirectMailRequest(
        @NotBlank @Size(max = 255) String subject,
        @NotBlank String body,
        @NotNull List<Long> recipientIds
) {
}
