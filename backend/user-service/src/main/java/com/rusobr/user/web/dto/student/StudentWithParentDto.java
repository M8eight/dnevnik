package com.rusobr.user.web.dto.student;

import com.rusobr.user.web.dto.user.UserResponse;

public record StudentWithParentDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        String keycloakId,
        UserResponse parent
) {
}
