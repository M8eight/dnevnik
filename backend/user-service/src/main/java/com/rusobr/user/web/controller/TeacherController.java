package com.rusobr.user.web.controller;

import com.rusobr.user.application.service.teacher.TeacherService;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherInfoResponse;
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
    public TeacherResponse getWithUserById(@PathVariable Long id) {
        return teacherService.getWithUserById(id);
    }

    @GetMapping("/{id}/details")
    public TeacherDetails getDetailsById(@PathVariable Long id) {
        return teacherService.getDetailsById(id);
    }

    @GetMapping("/{id}/info")
    public TeacherInfoResponse getInfoById(@PathVariable Long id) {
        return teacherService.getInfoById(id);
    }

    @PostMapping("/batch")
    public BatchUserResponse findBatchTeachers(@RequestBody List<Long> ids) {
        return teacherService.getBatch(ids);
    }

    @GetMapping("/{id}/simple")
    public UserFeignResponse getTeacherSimpleById(@PathVariable Long id) {
        return teacherService.getSimpleById(id);
    }

}
