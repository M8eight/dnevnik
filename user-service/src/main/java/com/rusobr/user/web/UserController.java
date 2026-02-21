package com.rusobr.user.web;

import com.rusobr.user.infrastructure.webClient.KeycloackRestClient;
import com.rusobr.user.web.dto.CreateKeyCloackUserRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final KeycloackRestClient keycloackRestClient;

    @GetMapping("/create")
    public String createUser() {
        CreateKeyCloackUserRequestDto dto = CreateKeyCloackUserRequestDto.builder().username("admin").password("admin").build();
        String keycloackId =  keycloackRestClient.createKeyCloackUser(dto);
        log.info("{} keycloack id", keycloackId);
        return "Roma porno besplatno";
    }

    @GetMapping("/jwt")
    public String getJwt(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getSubject();
    }

    @GetMapping("/admin")
    public String getAdmin() {
        return "Admin";
    }
}
