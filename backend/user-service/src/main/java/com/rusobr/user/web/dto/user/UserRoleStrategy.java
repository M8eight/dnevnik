package com.rusobr.user.web.dto.user;

import com.rusobr.user.domain.enums.UserRole;

public interface UserRoleStrategy {
    void save(Long userId, UserProfileDetails userDetails);
    void delete(Long userId);
    void update(Long userId, UserProfileDetails userDetails);
    UserRole getRole();
}
