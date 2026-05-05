package com.rusobr.user.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserCreateRequest(
        @NotNull String username,
        @NotNull String password,
        @NotNull String firstName,
        @NotNull String lastName
) {
}
