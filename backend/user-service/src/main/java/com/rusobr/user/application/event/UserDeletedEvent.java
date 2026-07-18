package com.rusobr.user.application.event;

import com.rusobr.common.enums.UserRole;

import java.util.Set;

public record UserDeletedEvent(
        Long id,
        Set<UserRole> roles
) {
}
