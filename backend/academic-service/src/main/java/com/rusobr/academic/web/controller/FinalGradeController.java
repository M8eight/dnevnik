package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.FinalGradeService;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeTeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/final-grades")
public class FinalGradeController {

    private final FinalGradeService finalGradeService;

    @PreAuthorize("@teacherSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/by-assignment")
    public FinalGradeTeacherResponse getByAssignmentId(@RequestParam Long teachingAssignmentId,
                                                       @RequestParam Long academicYearId) {
        return finalGradeService.getByAssignmentId(teachingAssignmentId, academicYearId);
    }

    @PreAuthorize("@teacherSecurity.canCreateFinalGrade(#finalGradeRequest.teachingAssignmentId(), #finalGradeRequest.studentId(), authentication)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinalGradeCreateResponse create(@RequestBody FinalGradeRequest finalGradeRequest) {
        return finalGradeService.create(finalGradeRequest);
    }

    @PreAuthorize("@teacherSecurity.canDeleteFinalGrade(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        finalGradeService.delete(id);
    }

}
