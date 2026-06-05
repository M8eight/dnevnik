package com.rusobr.user.infrastructure.client.webClient.keycloak;

import java.util.List;

public record KeycloakUserRequest(
        String username,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        List<KeycloakCredential> credentials
) {
}
