package com.rusobr.academic.web.dto.userService;

public record UserResponse(
        String firstName,
        String lastName,
        String keycloakId,
        Long id
) {
}
