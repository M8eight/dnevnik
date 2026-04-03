package com.rusobr.user.web.dto.keycloak.role;

import com.rusobr.user.infrastructure.enums.UserRoles;

public record AssignRoleToUserRequest(
         String keycloakId,
         UserRoles roleName,
         String roleId
) {
}
