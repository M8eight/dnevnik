package com.rusobr.user.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/users")
public class UserController {
    @GetMapping("/")
    public String getAll() {
        return "Roma porno besplatno";
    }
}
