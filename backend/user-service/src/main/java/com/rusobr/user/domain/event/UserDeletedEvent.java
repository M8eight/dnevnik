package com.rusobr.user.domain.event;

import com.rusobr.user.infrastructure.enums.UserRole;

import java.util.Set;

public record UserDeletedEvent(
        Long id,
        Set<UserRole> roles
) {
}
