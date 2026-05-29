package com.rusobr.user.web.dto.feign;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String username,
        String keycloakId
) {
}
