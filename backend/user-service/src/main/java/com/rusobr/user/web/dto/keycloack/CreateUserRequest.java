package com.rusobr.user.web.dto.keycloack;

import lombok.Builder;

@Builder
public record CreateUserRequest(
        String username,
        String password,
        String firstName,
        String lastName
) {
}
