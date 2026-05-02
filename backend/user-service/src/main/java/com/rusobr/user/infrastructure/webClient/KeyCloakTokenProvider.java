package com.rusobr.user.infrastructure.webClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class KeyCloakTokenProvider {
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    private String accessToken;
    private long expireTime;

    private final WebClient webClient;

    public KeyCloakTokenProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public synchronized String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() > expireTime) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);

            Map<String, Object> res = webClient.post()
                    .uri(keycloakUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Keycloak error: " + body))
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if(res == null || res.get("access_token") == null) {
                throw new IllegalStateException("Failed to get access token");
            }
            accessToken = (String) res.get("access_token");
            long expiresIn = ((Number) res.get("expires_in")).longValue();
            expireTime = System.currentTimeMillis() + (expiresIn - 30) * 1000L;
        }
        return accessToken;
    }
}
