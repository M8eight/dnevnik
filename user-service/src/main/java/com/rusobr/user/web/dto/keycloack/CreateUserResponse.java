package com.rusobr.user.web.dto.keycloack;

import lombok.Builder;

@Builder
public record CreateUserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    String keycloackId
) {}
