package com.rusobr.user.web.dto.keycloak.role;

public record AssignRoleToUserRequest(
         String keycloakId,
         String roleName,
         String roleId
) {
}
