package com.rusobr.user.web.dto.user;

import com.rusobr.common.enums.UserRole;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    String keycloakId,
    Set<UserRole> roles
) {}
