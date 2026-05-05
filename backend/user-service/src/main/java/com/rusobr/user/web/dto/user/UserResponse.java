package com.rusobr.user.web.dto.user;

import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    String keycloakId
) {}
