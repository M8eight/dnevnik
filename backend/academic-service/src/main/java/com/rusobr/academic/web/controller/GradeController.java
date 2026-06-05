package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/avg/by-student/{id}")
    public Double getAverageByPeriod(@PathVariable Long id, @RequestParam @NotNull Long academicPeriodId) {
        return gradeService.getAverageByPeriod(id, academicPeriodId);
    }

    @GetMapping("/by-date")
    public List<GradeWithSubjectNameResponse> findAllByDate(@RequestParam @NotNull Long studentId,
                                                            @RequestParam @NotNull LocalDate date) {
        return gradeService.findAllByDate(studentId, date);
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
