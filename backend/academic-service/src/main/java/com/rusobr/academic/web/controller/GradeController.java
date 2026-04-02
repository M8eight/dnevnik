package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/{id}")
    public GradeResponse getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id);
    }

    @PostMapping
    public CreateGradeResponse createGrade(@RequestBody @Valid CreateGradeRequest createGradeRequest) {
        return gradeService.createGrade(createGradeRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteGradeById(@PathVariable Long id) {
        gradeService.deleteGrade(id);
    }
}
