package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.TeacherAcademicService;
import com.rusobr.academic.web.dto.feign.TeacherInfoFeignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherAcademicController {

    private final TeacherAcademicService teacherAcademicService;

    @GetMapping("/{id}/info")
    public TeacherInfoFeignResponse getTeacherAcademicInfo(@PathVariable Long id) {
        return teacherAcademicService.getTeacherAcademicInfo(id);
    }

}
