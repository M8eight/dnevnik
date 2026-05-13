package com.rusobr.user.infrastructure.webClient;

import com.rusobr.user.infrastructure.exception.KeycloakUserAlreadyExist;
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

import java.util.HashMap;
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

    public KeycloakUserResponse getKeycloakUserByUsername(String username) throws KeycloakUserAlreadyExist {

        List<KeycloakUserResponse> userList = webClient.get().uri(keycloakUrl + "/admin/realms/" +
                        keycloakRealm + "/users" + "?username=" + username + "&exact=true")
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .collectList()
                .block();

        return Objects.requireNonNull(userList).get(0);
    }

    public String createKeyCloakUser(UserDataDto createUserRequest) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", createUserRequest.username());
        user.put("firstName", createUserRequest.firstName());
        user.put("lastName", createUserRequest.lastName());
        user.put("enabled", true);
        user.put("emailVerified", true);
        user.put("credentials", List.of(Map.of(
                "type", "password",
                "value", createUserRequest.password(),
                "temporary", false
        )));

        try {
            return webClient.post()
                    .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                    .bodyValue(user)
                    .retrieve()
                    .onStatus(status -> status.value() == 409,
                            clientResponse -> Mono.error(new KeycloakUserAlreadyExist(createUserRequest.username())
                    ))
                    .toBodilessEntity()
                    .flatMap(res -> {
                        String location = res.getHeaders().getFirst("Location");
                        return Mono.just(Objects.requireNonNull(location).substring(location.lastIndexOf("/") + 1));
                    })
                    .block();
        } catch (Exception e) {
            log.error("Failed to create/get Keycloak user: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteKeyCloakUser(String userId) {
        webClient.delete()
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/users/" + userId)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
                )
                .bodyToMono(Void.class)
                .block();

    }

    public void updateKeycloakUserProfile(String userId, UserUpdateData dto) {
        Map<String, Object> userDto = new HashMap<>();
        if (dto.username() != null) userDto.put("username", dto.username());
        if (dto.firstName() != null) userDto.put("firstName", dto.firstName());
        if (dto.lastName() != null) userDto.put("lastName", dto.lastName());

        if (userDto.isEmpty()) return;

        webClient.put()
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/users/" + userId)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                    resp.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
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
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/users/" + userId + "/reset-password")
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
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/roles")
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Keycloak error: " + body))
                )
                .bodyToFlux(KeycloakRole.class)
                .collectList()
                .block();
    }

    public void assignRoleToUser(AssignRoleToUserRequest dto) {
        Map<String, Object> role = Map.of(
                "id", dto.roleId(),
                "name", dto.roleName()
        );

        webClient.post()
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/users/" + dto.keycloakId() + "/role-mappings/realm")
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        clientResponse -> Mono.error(new KeycloakUserAlreadyExist("Keycloak User role already assigned")))
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
                )
                .bodyToMono(Void.class)
                .block();

    }

    public void deleteRoleFromUser(AssignRoleToUserRequest dto) {
        Map<String, Object> role = Map.of(
                "id", dto.roleId(),
                "name", dto.roleName()
        );


        webClient.method(HttpMethod.DELETE)
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/users/" + dto.keycloakId() + "/role-mappings/realm")
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public KeycloakRole getRoleByName(String roleName) {
        return webClient.get()
                .uri(keycloakUrl + "/admin/realms/" + keycloakRealm + "/roles/" + roleName)
                .header("Authorization", "Bearer " + keyCloakTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
                )
                .bodyToMono(KeycloakRole.class)
                .block();
    }
}
