package com.rusobr.user.infrastructure.service.user;

import com.rusobr.user.infrastructure.enums.UserRole;

public interface CreateUserStrategy {
    void save(Long userId, UserProfileDetails userDetails);
    UserRole getRole();
}
