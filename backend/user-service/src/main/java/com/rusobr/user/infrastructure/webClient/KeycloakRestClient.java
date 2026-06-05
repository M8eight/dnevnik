package com.rusobr.user.infrastructure.webClient;

import com.rusobr.user.infrastructure.exception.KeycloakUserAlreadyExist;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.webClient.keycloak.KeycloakCredential;
import com.rusobr.user.infrastructure.webClient.keycloak.KeycloakRoleRequest;
import com.rusobr.user.infrastructure.webClient.keycloak.KeycloakUpdateRequest;
import com.rusobr.user.infrastructure.webClient.keycloak.KeycloakUserRequest;
import com.rusobr.user.web.dto.keycloak.KeycloakUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class KeycloakRestClient {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    private final KeyCloakTokenProvider keyCloakTokenProvider;

    private final WebClient webClient;

    public KeycloakRestClient(KeyCloakTokenProvider keyCloakTokenProvider, WebClient.Builder webClientBuilder) {
        this.keyCloakTokenProvider = keyCloakTokenProvider;
        this.webClient = webClientBuilder.build();
    }

    public KeycloakUserResponse getKeycloakUserByUsername(String username) {
        KeycloakUserResponse user = webClient.get()
                .uri("{url}/admin/realms/{realm}/users?username={username}&exact=true", keycloakUrl, keycloakRealm, username)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .next()
                .block();

        if (user == null) {
            throw new NotFoundException("Keycloak User not found " + username);
        }
        return user;
    }

    public String createKeyCloakUser(UserDataDto dto) {
        KeycloakUserRequest user = new KeycloakUserRequest(
                dto.username(),
                dto.firstName(),
                dto.lastName(),
                true,
                true,
                List.of(new KeycloakCredential("password", dto.password(), false))
        );

            return webClient.post()
                    .uri("{url}/admin/realms/{realm}/users", keycloakUrl, keycloakRealm)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                    .bodyValue(user)
                    .retrieve()
                    .onStatus(status -> status.value() == 409,
                            clientResponse -> Mono.error(new KeycloakUserAlreadyExist(dto.username())
                    ))
                    .toBodilessEntity()
                    .flatMap(res -> {
                        String location = res.getHeaders().getFirst("Location");
                        return Mono.just(Objects.requireNonNull(location).substring(location.lastIndexOf("/") + 1));
                    })
                    .block();
    }

    public void deleteKeyCloakUser(String userId) {
        webClient.delete()
                .uri("{url}/admin/realms/{realm}/users/{userId}", keycloakUrl, keycloakRealm, userId)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public void updateKeycloakUserProfile(String userId, UserUpdateData dto) {
        if (dto.username() == null && dto.firstName() == null && dto.lastName() == null) {
            return;
        }

        KeycloakUpdateRequest updateRequest = new KeycloakUpdateRequest(
                dto.username(),
                dto.firstName(),
                dto.lastName()
        );

        webClient.put()
                .uri("{url}/admin/realms/{realm}/users/{userId}", keycloakUrl, keycloakRealm, userId)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                    resp.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public void resetKeycloakPassword(String userId, String newPassword) {
        Map<String, Object> credentials = Map.of(
                "type", "password",
                "value", newPassword,
                //TODO: Учесть в будущем про временный пароль
                "temporary", false
        );

        webClient.put()
                .uri("{url}/admin/realms/{realm}/users/{userId}/reset-password", keycloakUrl, keycloakRealm, userId)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public List<KeycloakRole> getAllKeycloakRoles() {
        return webClient.get()
                .uri("{url}/admin/realms/{realm}/roles", keycloakUrl, keycloakRealm)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak" + b)))
                )
                .bodyToFlux(KeycloakRole.class)
                .collectList()
                .block();
    }

    public void assignRoleToUser(AssignRoleToUserRequest dto) {
        KeycloakRoleRequest role = new KeycloakRoleRequest(dto.roleId(), dto.roleName());

        webClient.post()
                .uri("{url}/admin/realms/{realm}/users/{userId}/role-mappings/realm", keycloakUrl, keycloakRealm, dto.keycloakId())
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        clientResponse -> Mono.error(new KeycloakUserAlreadyExist("Keycloak User role already assigned")))
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public void deleteRoleFromUser(AssignRoleToUserRequest dto) {
        KeycloakRoleRequest role = new KeycloakRoleRequest(dto.roleId(), dto.roleName());

        webClient.method(HttpMethod.DELETE)
                .uri("{url}/admin/realms/{realm}/users/{userId}/role-mappings/realm", keycloakUrl, keycloakRealm, dto.keycloakId())
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public KeycloakRole getRoleByName(String roleName) {
        return webClient.get()
                .uri("{url}/admin/realms/{realm}/roles/{roleName}", keycloakUrl, keycloakRealm, roleName)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToMono(KeycloakRole.class)
                .block();
    }

}
