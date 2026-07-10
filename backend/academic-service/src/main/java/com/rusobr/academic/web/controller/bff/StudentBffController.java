package com.rusobr.academic.web.controller.bff;

import com.rusobr.academic.application.service.bff.StudentBffService;
import com.rusobr.academic.web.dto.bff.student.HomeAggregation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/bff/students")
@RequiredArgsConstructor
public class StudentBffController {

    private final StudentBffService studentBffService;

    @GetMapping("/home")
    public HomeAggregation getHomeAggregation(@RequestParam LocalDate date, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return studentBffService.getHomeAggregation(date, userId);
    }

}
