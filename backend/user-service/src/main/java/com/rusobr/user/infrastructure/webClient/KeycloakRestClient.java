package com.rusobr.user.infrastructure.webClient;

import com.rusobr.user.infrastructure.exception.KeycloackUserAlreadyExist;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.KeycloakUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@Slf4j
public class KeycloakRestClient {

    @Value("${keycloack.url}")
    private String keycloackUrl;

    @Value("${keycloack.realm}")
    private String keycloackRealm;

    private final KeyCloackTokenProvider keyCloackTokenProvider;

    private final WebClient webClient;

    public KeycloakRestClient(KeyCloackTokenProvider keyCloackTokenProvider, WebClient.Builder webClientBuilder) {
        this.keyCloackTokenProvider = keyCloackTokenProvider;
        this.webClient = webClientBuilder.build();
    }

//    public KeycloackUserResponse getKeycloackUser(String keycloakId) {
//        return webClient.get().uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" + keycloakId)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .bodyToMono(KeycloackUserResponse.class)
//                .block();
//    }

    public KeycloakUserResponse getKeycloakUserByUsername(String username) throws KeycloackUserAlreadyExist {

//        log.info("Token {}", keyCloackTokenProvider.getAccessToken());

        List<KeycloakUserResponse> userList = webClient.get().uri(keycloackUrl + "/admin/realms/" +
                keycloackRealm + "/users" + "?username=" + username + "&exact=true")
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .collectList()
                .block();

        return Objects.requireNonNull(userList).get(0);
    }

    public String createKeyCloakUser(CreateUserRequest createUserRequest) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", createUserRequest.username());
        user.put("enabled", true);
        user.put("emailVerified", true);
        user.put("credentials", List.of(Map.of(
                "type", "password",
                "value", createUserRequest.password(),
                "temporary", false
        )));

        try {
            return webClient.post()
                    .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                    .bodyValue(user)
                    .retrieve()
                    // Вместо Mono.error просто логируем и обрабатываем ниже
                    .onStatus(status -> status.value() == 409,
                            clientResponse -> Mono.empty())
                    .toBodilessEntity()
                    .flatMap(res -> {
                        if (res.getStatusCode().value() == 409) {
                            return Mono.empty();
                        }
                        String location = res.getHeaders().getFirst("Location");
                        return Mono.just(location.substring(location.lastIndexOf("/") + 1));
                    })
                    .blockOptional() // Используем Optional, чтобы обработать пустоту
                    .orElseGet(() -> {
                        log.info("User {} already exists, fetching existing ID", createUserRequest.username());
                        return getKeycloakUserByUsername(createUserRequest.username()).id();
                    });
        } catch (Exception e) {
            log.error("Failed to create/get Keycloak user: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteKeyCloackUser(String userId)
    {
        webClient.delete()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" + userId)
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
                )
                .bodyToMono(Void.class)
                .block();

    }

    public List<KeycloakRole> getAllKeycloackRoles() {
        return webClient.get()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/roles")
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
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
        Map<String,Object> role = Map.of(
                "id", dto.roleId(),
                    "name", dto.roleName()
        );

        webClient.post()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" +  dto.keycloakId() + "/role-mappings/realm")
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        clientResponse -> Mono.error(new KeycloackUserAlreadyExist("Keycloack User role already assigned")))
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Keycloak: " + body)))
                )
                .bodyToMono(Void.class)
                .block();

    }

    public void deleteRoleFromUser(AssignRoleToUserRequest dto) {
        Map<String,Object> role = Map.of(
                "id", dto.roleId(),
                "name", dto.roleName()
        );


        webClient.method(HttpMethod.DELETE)
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" +  dto.keycloakId() + "/role-mappings/realm")
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
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
}
