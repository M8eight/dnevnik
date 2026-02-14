package com.rusobr.user.infrastructure.webClient;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class KeyCloackTokenProvider {
    @Value("${keycloack.client-secret}")
    private String clientSecret;

    @Value("${keycloack.client-id}")
    private String clientId;

    @Value("${keycloack.url}")
    private String keycloackUrl;

    @Value("${keycloack.realm}")
    private String keycloackRealm;

    private String accessToken;
    private long expireTime;

    private final WebClient webClient;

    public KeyCloackTokenProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public synchronized String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() > expireTime) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);

            Map<String, Object> res = webClient.post()
                    .uri(keycloackUrl + "/realms/" + keycloackRealm + "/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Keycloak error: " + body))
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            assert res != null;
            accessToken = (String) res.get("access_token");
            log.info("Access token polychit: {}", accessToken);
            long expiresIn = ((Number) res.get("expires_in")).longValue();
            expireTime = System.currentTimeMillis() + (expiresIn - 5) * 1000L;
        }
        return accessToken;
    }
}
