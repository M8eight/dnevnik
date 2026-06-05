package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.TeacherSubjectService;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectRequest;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher-subjects")
@RequiredArgsConstructor
public class TeacherSubjectController {

    private final TeacherSubjectService teacherSubjectService;

    @GetMapping
    public List<TeacherSubjectResponse> findAll() {
        return teacherSubjectService.findAll();
    }

    @PostMapping
    public TeacherSubjectResponse create(@RequestBody @Valid TeacherSubjectRequest request) {
        return teacherSubjectService.create(request);
    }

    @DeleteMapping
    public void delete(@RequestBody @Valid TeacherSubjectRequest request) {
        teacherSubjectService.delete(request);
    }

}
