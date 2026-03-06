package com.rusobr.user.web.dto.keycloack.role;

import com.rusobr.user.infrastructure.enums.UserRoles;

public record AssignRoleToUserRequest(
         String keycloackId,
         UserRoles roleName,
         String roleId
) {
}
