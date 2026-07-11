package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teaching-assignments")
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    @GetMapping
    public List<TeachingAssignmentDetailsDto> getByTeacherId(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return teachingAssignmentService.getByTeacherId(userId);
    }

}
