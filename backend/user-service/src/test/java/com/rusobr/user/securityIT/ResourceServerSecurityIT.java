package com.rusobr.user.securityIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceServerSecurityIT extends AbstractSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Запрос без токена -> 401")
    void noToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Битый токен -> 401")
    void invalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Подписанный, но просроченный токен -> 401")
    void expiredToken_ShouldReturn401() throws Exception {
        String expiredToken = JwtTestUtils.expiredToken(27L, List.of("ADMIN"));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Валидный токен с нужной ролью -> 200")
    void validTokenWithRequiredRole_ShouldReturn200() throws Exception {
        String adminToken = JwtTestUtils.token(1L, List.of("ADMIN"));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Валидный токен, но недостаточная роль -> 403")
    void validTokenWithInsufficientRole_ShouldReturn403() throws Exception {
        String studentToken = JwtTestUtils.token(27L, List.of("STUDENT"));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Валидный токен без ролей -> 403")
    void validTokenWithoutRoles_ShouldReturn403() throws Exception {
        String noRolesToken = JwtTestUtils.token(Map.of("user_id", 27L), Duration.ofHours(1));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + noRolesToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Публичный actuator/health без токена -> 200")
    void publicActuatorHealth_WithoutToken_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Публичный /public/** не блокируется security (не 401/403)")
    void publicPath_WithoutToken_IsNotBlockedBySecurity() throws Exception {
        MvcResult result = mockMvc.perform(get("/public/nonexistent"))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("public path must not be blocked by security")
                .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Незамапленный URL без токена -> 401")
    void unmappedUrl_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/nonexistent-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Незамапленный URL с валидным токеном -> 403 (anyRequest.denyAll)")
    void unmappedUrl_WithToken_ShouldReturn403() throws Exception {
        String studentToken = JwtTestUtils.token(27L, List.of("STUDENT"));

        mockMvc.perform(get("/api/v1/nonexistent-endpoint")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

}
