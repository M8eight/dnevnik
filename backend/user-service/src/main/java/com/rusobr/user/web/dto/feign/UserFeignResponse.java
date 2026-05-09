package com.rusobr.user.web.dto.feign;

public record UserFeignResponse(
        Long id,
        String firstName,
        String lastName,
        String username,
        String keycloakId
) {
}
