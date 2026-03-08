package com.rusobr.user.infrastructure.webClient;

import com.rusobr.user.infrastructure.exception.KeycloackUserAlreadyExist;
import com.rusobr.user.web.dto.keycloack.CreateUserRequest;
import com.rusobr.user.web.dto.keycloack.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloack.role.KeycloackRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class KeycloackRestClient {

    @Value("${keycloack.url}")
    private String keycloackUrl;

    @Value("${keycloack.realm}")
    private String keycloackRealm;

    private final KeyCloackTokenProvider keyCloackTokenProvider;

    private final WebClient webClient;

    public KeycloackRestClient(KeyCloackTokenProvider keyCloackTokenProvider, WebClient.Builder webClientBuilder) {
        this.keyCloackTokenProvider = keyCloackTokenProvider;
        this.webClient = webClientBuilder.build();
    }

//    public KeycloackUserResponse getKeycloackUser(String keycloackId) {
//        return webClient.get().uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" + keycloackId)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .bodyToMono(KeycloackUserResponse.class)
//                .block();
//    }

    public String createKeyCloackUser(CreateUserRequest createUserRequest) {

        //todo map this
        Map<String,Object> user = new HashMap<>();
        user.put("username", createUserRequest.username());
        user.put("enabled", true);
        user.put("emailVerified", true);

        List<Map<String, Object>> credentials = new ArrayList<>();
        Map<String, Object> password = Map.of(
                "type", "password",
                "value", createUserRequest.password(),
                "temporary", false
        );
        credentials.add(password);
        user.put("credentials", credentials);

        return webClient.post()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                .bodyValue(user)
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        clientResponse -> Mono.error(new KeycloackUserAlreadyExist("Keycloack User already exists")))
                .toBodilessEntity()
                .map(res -> {
                    String location = res.getHeaders().getFirst("Location");
                    if (location == null) {
                        throw new RuntimeException("Location header not found");
                    }
                    return location.substring(location.lastIndexOf("/") + 1);
                })
                .block();
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

    public List<KeycloackRole> getAllKeycloackRoles() {
        return webClient.get()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/roles")
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Keycloak error: " + body))
                )
                .bodyToFlux(KeycloackRole.class)
                .collectList()
                .block();
    }

    public void assignRoleToUser(AssignRoleToUserRequest dto) {
        Map<String,Object> role = Map.of(
                "id", dto.roleId(),
                    "name", dto.roleName()
        );

        webClient.post()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" +  dto.keycloackId() + "/role-mappings/realm")
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

    public void deleteRoleFromUser(AssignRoleToUserRequest dto) {
        Map<String,Object> role = Map.of(
                "id", dto.roleId(),
                "name", dto.roleName()
        );


        webClient.method(HttpMethod.DELETE)
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" +  dto.keycloackId() + "/role-mappings/realm")
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
