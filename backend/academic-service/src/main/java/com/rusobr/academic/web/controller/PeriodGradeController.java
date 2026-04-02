package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.PeriodGradeService;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeProjection;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/period/grades")
public class PeriodGradeController {
    private final PeriodGradeService periodGradeService;

    @GetMapping("/class")
    public List<StudentPeriodGradeResponse> findBySchoolClassId(@RequestParam Long teachingAssignmentId, @RequestParam Long academicPeriodId) {
        return periodGradeService.findBySchoolClassId(teachingAssignmentId, academicPeriodId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodGradeResponse createPeriodGrade(@RequestBody PeriodGradeRequest periodGradeRequest) {
        return periodGradeService.createGrade(periodGradeRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePeriodGrade(@PathVariable Long id) {
        periodGradeService.deletePeriodGrade(id);
    }

}
