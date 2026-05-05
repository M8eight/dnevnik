package com.rusobr.user.web.dto.user;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;

public record UserRequest<T extends UserProfileDetails> (
        UserCreateRequest user,
        UserRole role,
        T details
) {}
