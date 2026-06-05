package com.rusobr.user.infrastructure.client.webClient.keycloak;

public record KeycloakCredential(
        String type,
        String value,
        boolean temporary
) {
}
