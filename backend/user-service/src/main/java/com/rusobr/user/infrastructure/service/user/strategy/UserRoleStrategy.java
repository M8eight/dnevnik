package com.rusobr.user.infrastructure.service.user.strategy;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;

public interface UserRoleStrategy {
    void save(Long userId, UserProfileDetails userDetails);
    void delete(Long userId);
    void update(Long userId, UserProfileDetails userDetails);
    UserRole getRole();
}
