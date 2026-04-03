package com.rusobr.user.web.dto.student;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String keycloakId
) {}
