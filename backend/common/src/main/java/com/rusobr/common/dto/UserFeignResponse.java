package com.rusobr.common.dto;

import lombok.Builder;

@Builder
public record UserFeignResponse(
        Long id,
        String firstName,
        String lastName,
        String username,
        String keycloakId
) {
}
