package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeDetailResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/{id}")
    public GradeResponse getById(@PathVariable Long id) {
        return gradeService.getById(id);
    }

    @PreAuthorize("@gradeSecurity.canViewStudent(#id, authentication)")
    @GetMapping("/{id}/detail")
    public GradeDetailResponse getDetailById(@PathVariable Long id) {
        return gradeService.getDetail(id);
    }

    @PostMapping
    public CreateGradeResponse create(@RequestBody @Valid CreateGradeRequest createGradeRequest) {
        return gradeService.create(createGradeRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        gradeService.delete(id);
    }
}
