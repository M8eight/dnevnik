package com.rusobr.user.infrastructure.client.webClient;

import com.rusobr.user.infrastructure.client.webClient.keycloak.KeycloakCredential;
import com.rusobr.user.infrastructure.client.webClient.keycloak.KeycloakRoleRequest;
import com.rusobr.user.infrastructure.client.webClient.keycloak.KeycloakUpdateRequest;
import com.rusobr.user.infrastructure.client.webClient.keycloak.KeycloakUserRequest;
import com.rusobr.user.web.dto.keycloak.KeycloakUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import com.rusobr.user.web.exception.KeycloakUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rusobr.user.web.exception.UserExceptionCode.KEYCLOAK_USER_BAD_REQUEST;
import static com.rusobr.user.web.exception.UserExceptionCode.KEYCLOAK_USER_NOT_FOUND;

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

    private String token() {
        return "Bearer " + keyCloakTokenProvider.getAccessToken();
    }

    public KeycloakUserResponse getKeycloakUserByUsername(String username) {
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users")
                .queryParam("username", username)
                .queryParam("exact", true)
                .buildAndExpand(keycloakRealm)
                .toUriString();

        KeycloakUserResponse user = webClient.get()
                .uri(uri)
                .header("Authorization", token())
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .next()
                .block();

        if (user == null) {
            throw new KeycloakUserException("Keycloak user %s not found".formatted(username), KEYCLOAK_USER_NOT_FOUND);
        }

        return user;
    }

    public String createKeyCloakUser(UserDataDto dto, Long userId) {
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users")
                .buildAndExpand(keycloakRealm)
                .toUriString();

        Map<String, List<String>> userAttributes = Map.of("userId", List.of(userId.toString()));

        KeycloakUserRequest user = new KeycloakUserRequest(
                dto.username(),
                dto.firstName(),
                dto.lastName(),
                true,
                true,
                List.of(new KeycloakCredential("password", dto.password(), false)),
                userAttributes
        );

        return webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token())
                .bodyValue(user)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak create user: " + b)))
                )
                .toBodilessEntity()
                .flatMap(res -> {
                    String location = res.getHeaders().getFirst("Location");
                    return Mono.just(Objects.requireNonNull(location).substring(location.lastIndexOf("/") + 1));
                })
                .block();
    }

    public void deleteKeyCloakUser(String userId) {
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users/{userId}")
                .buildAndExpand(keycloakRealm, userId)
                .toUriString();

        webClient.delete()
                .uri(uri)
                .header("Authorization", token())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak delete user: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public void updateKeycloakUserProfile(String userId, UserUpdateData dto) {
        if (dto.username() == null && dto.firstName() == null && dto.lastName() == null) {
            return;
        }

        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users/{userId}")
                .buildAndExpand(keycloakRealm, userId)
                .toUriString();

        KeycloakUpdateRequest updateRequest = new KeycloakUpdateRequest(
                dto.username(),
                dto.firstName(),
                dto.lastName()
        );

        webClient.put()
                .uri(uri)
                .header("Authorization", token())
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
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users/{userId}/reset-password")
                .buildAndExpand(keycloakRealm, userId)
                .toUriString();

        List<KeycloakCredential> credentials = List.of(new KeycloakCredential("password", newPassword, false));

        webClient.put()
                .uri(uri)
                .header("Authorization", token())
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
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/roles")
                .buildAndExpand(keycloakRealm)
                .toUriString();

        return webClient.get()
                .uri(uri)
                .header("Authorization", token())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak: " + b)))
                )
                .bodyToFlux(KeycloakRole.class)
                .collectList()
                .block();
    }

    public void assignRoleToUser(AssignRoleToUserRequest dto) {
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users/{userId}/role-mappings/realm")
                .buildAndExpand(keycloakRealm, dto.keycloakId())
                .toUriString();

        KeycloakRoleRequest role = new KeycloakRoleRequest(dto.roleId(), dto.roleName());

        webClient.post()
                .uri(uri)
                .header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        clientResponse -> Mono.error(new KeycloakUserException("Keycloak user role already assigned", KEYCLOAK_USER_BAD_REQUEST)))
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak assign role: " + b)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public void deleteRoleFromUser(AssignRoleToUserRequest dto) {
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/users/{userId}/role-mappings/realm")
                .buildAndExpand(keycloakRealm, dto.keycloakId())
                .toUriString();

        KeycloakRoleRequest role = new KeycloakRoleRequest(dto.roleId(), dto.roleName());

        webClient.method(HttpMethod.DELETE)
                .uri(uri)
                .header("Authorization", token())
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
        String uri = UriComponentsBuilder.fromUriString(keycloakUrl)
                .path("/admin/realms/{realm}/roles/{roleName}")
                .buildAndExpand(keycloakRealm, roleName)
                .toUriString();

        return webClient.get()
                .uri(uri)
                .header("Authorization", token())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("Keycloak get role by name: " + b)))
                )
                .bodyToMono(KeycloakRole.class)
                .block();
    }
}