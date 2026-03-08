package com.rusobr.user.web.dto.user;

public record UserResponse(
        String firstName,
        String lastName,
        String keycloackId,
        Long id
) {
}
