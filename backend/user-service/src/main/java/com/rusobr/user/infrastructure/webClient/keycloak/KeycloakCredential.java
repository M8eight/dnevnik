package com.rusobr.user.infrastructure.webClient.keycloak;

public record KeycloakCredential(
        String type,
        String value,
        boolean temporary
) {
}
