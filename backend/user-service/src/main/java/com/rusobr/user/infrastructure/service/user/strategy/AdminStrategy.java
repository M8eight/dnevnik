package com.rusobr.user.infrastructure.service.user.strategy;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.web.dto.user.UserProfileDetails;
import com.rusobr.user.web.dto.user.UserRoleStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AdminStrategy implements UserRoleStrategy {
    @Override
    public void save(Long userId, UserProfileDetails userDetails) {}

    @Override
    public void delete(Long userId) {}

    @Override
    public void update(Long userId, UserProfileDetails userDetails) {}

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }
}
