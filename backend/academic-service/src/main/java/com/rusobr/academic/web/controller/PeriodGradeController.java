package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.PeriodGradeService;
import com.rusobr.academic.web.dto.grade.StudentAverageResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/period/grades")
public class PeriodGradeController {
    private final PeriodGradeService periodGradeService;

    @GetMapping("/class")
    public Map<String, List<PeriodGradeStudentResponse>> getBySchoolClassId(@RequestParam Long studentId) {
        return periodGradeService.findBySchoolClassId(studentId);
    }

    @GetMapping("/by-teaching-assignment/with-avg")
    public List<StudentAverageResponse> getAverageGradesByClass(@RequestParam Long teachingAssignmentId,
                                                                @RequestParam Long academicPeriodId) {
        return periodGradeService.getStudentPeriodGradesWithAverage(teachingAssignmentId, academicPeriodId);
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
