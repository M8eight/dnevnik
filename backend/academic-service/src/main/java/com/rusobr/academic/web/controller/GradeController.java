package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/{id}")
    public GradeResponse getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id);
    }

    @GetMapping("/avg/by-student/{id}")
    public Double getAverageGrade(@PathVariable Long id, @RequestParam @NotNull Long academicPeriodId) {
        log.info("getAverageGrade {} {}", id, academicPeriodId);
        return gradeService.getAverageGrade(id, academicPeriodId);
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
