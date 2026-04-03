package com.rusobr.user.web.dto.teacher;

public record TeacherResponse(
        Long id,
        Long userId,
        String keycloakId,
        String firstName,
        String lastName,
        String phoneNumber,
        String email
) {
}
