package com.rusobr.user.web.dto.keycloak;

import lombok.Builder;

@Builder
public record CreateUserRequest(
        String username,
        String password,
        String firstName,
        String lastName
) {
}
