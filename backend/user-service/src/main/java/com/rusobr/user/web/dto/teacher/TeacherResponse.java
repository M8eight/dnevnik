package com.rusobr.user.web.dto.teacher;

public record TeacherResponse(
        Long id,
        String keycloakId,
        String firstName,
        String lastName,
        String phoneNumber,
        String email
) {
}
