package com.rusobr.user.web.dto.parent;

import com.rusobr.user.web.dto.user.UserResponse;

import java.util.List;

public record ParentInfoResponse(
    List<UserResponse> children
) {
}
