package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/{id}")
    public TeacherResponse findById(@PathVariable Long id) {
        return teacherService.findWithUserById(id);
    }

    @GetMapping("/{id}/details")
    public TeacherDetails findDetailsById(@PathVariable Long id) {
        return teacherService.findDetailsById(id);
    }

}
