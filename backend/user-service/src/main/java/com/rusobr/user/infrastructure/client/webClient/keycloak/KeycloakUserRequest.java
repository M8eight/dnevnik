package com.rusobr.user.infrastructure.client.webClient.keycloak;

import java.util.List;
import java.util.Map;

public record KeycloakUserRequest(
        String username,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        List<KeycloakCredential> credentials,
        Map<String, List<String>> attributes
) {
}
