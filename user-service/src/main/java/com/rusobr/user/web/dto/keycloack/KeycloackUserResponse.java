package com.rusobr.user.web.dto.keycloack;

import lombok.Builder;

@Builder
public record KeycloackUserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    String keycloackId
) {}
