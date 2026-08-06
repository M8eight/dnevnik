package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.PeriodGradeService;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeTeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/period-grades")
public class PeriodGradeController {
    private final PeriodGradeService periodGradeService;

    @PreAuthorize("@teacherSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/by-assignment")
    public PeriodGradeTeacherResponse getGradesByAssignment(@RequestParam Long teachingAssignmentId,
                                                                     @RequestParam Long currentAcademicPeriodId,
                                                                     @RequestParam Long academicYearId) {
        return periodGradeService.getByAssignmentWithAverage(teachingAssignmentId, currentAcademicPeriodId, academicYearId);
    }

    @PreAuthorize("@teacherSecurity.canCreatePeriodGrade(#periodGradeRequest.teachingAssignmentId(), #periodGradeRequest.studentId(), authentication)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodGradeResponse createPeriodGrade(@RequestBody PeriodGradeRequest periodGradeRequest) {
        return periodGradeService.create(periodGradeRequest);
    }


    @PreAuthorize("@teacherSecurity.canDeletePeriodGrade(#periodGradeRequest.teachingAssignmentId(), #periodGradeRequest.studentId(), authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePeriodGrade(@PathVariable Long id) {
        periodGradeService.delete(id);
    }

}
