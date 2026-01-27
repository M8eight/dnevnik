package com.rusobr.user.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/")
    public String getAll() {
        log.info("delay geev");
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
