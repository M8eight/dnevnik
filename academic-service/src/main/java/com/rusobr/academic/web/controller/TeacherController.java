package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.TeacherService;
import com.rusobr.academic.web.dto.userService.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class TeacherController {
    private final TeacherService teacherService;

    @GetMapping("/class/{id}")
    public Set<UserResponse> getUsersFromClass(@PathVariable Long id) {
        return teacherService.getUsersIdFromClass(id);
    }
}
