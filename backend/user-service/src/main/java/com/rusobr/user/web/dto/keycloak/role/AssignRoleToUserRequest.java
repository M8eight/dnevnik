package com.rusobr.user.web.dto.keycloak.role;

import com.rusobr.user.infrastructure.enums.UserRole;

public record AssignRoleToUserRequest(
         String keycloakId,
         UserRole roleName,
         String roleId
) {
}
