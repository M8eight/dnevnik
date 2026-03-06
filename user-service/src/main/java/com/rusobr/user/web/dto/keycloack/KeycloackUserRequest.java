package com.rusobr.user.web.dto.keycloack;

import lombok.Builder;

@Builder
public record KeycloackUserRequest(
        String username,
        String password,
        String firstName,
        String lastName
) {
}
