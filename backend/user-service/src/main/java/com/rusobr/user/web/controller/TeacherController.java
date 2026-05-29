package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.web.dto.feign.UserResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/batch")
    public List<UserResponse> findBatchTeachers(@RequestBody List<Long> ids) {
        return teacherService.getSimpleBatchTeachers(ids);
    }

    @GetMapping("/{id}/simple")
    public UserResponse getTeacherSimpleById(@PathVariable Long id) {
        return teacherService.getTeacherSimpleById(id);
    }

}
