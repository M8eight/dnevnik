package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.PeriodGradeService;
import com.rusobr.academic.web.dto.grade.periodGrade.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/period-grades")
public class PeriodGradeController {
    private final PeriodGradeService periodGradeService;

    @GetMapping("/by-student")
    public Map<String, List<PeriodGradeStudentResponse>> getByStudentId(@RequestParam Long studentId,
                                                                        @RequestParam String schoolYear) {
        return periodGradeService.getByStudentId(studentId, schoolYear);
    }

    @GetMapping("/by-assignment")
    public List<PeriodGradeTeacherResponse> getGradesByAssignment(@RequestParam Long teachingAssignmentId,
                                                                  @RequestParam Long currentAcademicPeriodId,
                                                                  @RequestParam String schoolYear) {
        return periodGradeService.getByAssignmentWithAverage(teachingAssignmentId, currentAcademicPeriodId, schoolYear);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodGradeResponse createPeriodGrade(@RequestBody PeriodGradeRequest periodGradeRequest) {
        return periodGradeService.create(periodGradeRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePeriodGrade(@PathVariable Long id) {
        periodGradeService.delete(id);
    }

}
