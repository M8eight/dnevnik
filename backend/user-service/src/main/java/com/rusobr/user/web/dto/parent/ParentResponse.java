package com.rusobr.user.web.dto.parent;

import com.rusobr.user.web.dto.user.UserResponse;

import java.util.Set;

public record ParentResponse(
        UserResponse user,
        Set<UserResponse> children
) {
}
