package com.rusobr.user.infrastructure.client.webClient.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KeycloakUpdateRequest(
        String username,
        String firstName,
        String lastName
) {
}
