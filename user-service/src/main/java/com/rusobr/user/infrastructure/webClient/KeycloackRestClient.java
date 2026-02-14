package com.rusobr.user.infrastructure.webClient;

import com.rusobr.user.web.dto.AssignRoleToUserRequestDto;
import com.rusobr.user.web.dto.CreateKeyCloackUserRequestDto;
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

    public String createKeyCloackUser(CreateKeyCloackUserRequestDto createKeyCloackUserDto) {

        //todo map this
        Map<String,Object> user = new HashMap<>();
        user.put("username", createKeyCloackUserDto.getUsername());
        user.put("enabled", true);
        user.put("emailVerified", true);

        List<Map<String, Object>> credentials = new ArrayList<>();
        Map<String, Object> password = Map.of(
                "type", "password",
                "value", createKeyCloackUserDto.getPassword(),
                "temporary", false
        );
        credentials.add(password);
        user.put("credentials", credentials);

        log.info("{}", user);

        return webClient.post()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + keyCloackTokenProvider.getAccessToken())
                .bodyValue(user)
                .retrieve()
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

    public void assignRoleToUser(AssignRoleToUserRequestDto dto) {
        Map<String,Object> role = Map.of(
                "id", dto.getRoleId(),
                    "name", dto.getRoleName()
        );

        webClient.post()
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" +  dto.getUserId() + "/role-mappings/realm")
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

    public void deleteRoleFromUser(AssignRoleToUserRequestDto dto) {
        Map<String,Object> role = Map.of(
                "id", dto.getRoleId(),
                "name", dto.getRoleName()
        );


        webClient.method(HttpMethod.DELETE)
                .uri(keycloackUrl + "/admin/realms/" + keycloackRealm + "/users/" +  dto.getUserId() + "/role-mappings/realm")
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
