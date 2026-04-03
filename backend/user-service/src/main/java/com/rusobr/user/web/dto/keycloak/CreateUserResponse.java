package com.rusobr.user.web.dto.keycloak;

import lombok.Builder;

@Builder
public record CreateUserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    String keycloakId
) {}
