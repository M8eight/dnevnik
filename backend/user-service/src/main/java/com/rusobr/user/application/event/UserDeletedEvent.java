package com.rusobr.user.application.event;

import com.rusobr.user.domain.enums.UserRole;

import java.util.Set;

public record UserDeletedEvent(
        Long id,
        Set<UserRole> roles
) {
}
