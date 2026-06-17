package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.FinalGradeService;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeTeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/final-grades")
public class FinalGradeController {

    private final FinalGradeService finalGradeService;

    @GetMapping("/by-student")
    public Map<String, FinalGradeResponse> getByStudentId(@RequestParam Long studentId, @RequestParam Long academicYearId) {
        return finalGradeService.getByStudentId(studentId, academicYearId);
    }

    @GetMapping("/by-assignment")
    public List<FinalGradeTeacherResponse> getByAssignmentId(@RequestParam Long teachingAssignmentId,
                                                             @RequestParam Long academicYearId) {
        return finalGradeService.getByAssignmentId(teachingAssignmentId, academicYearId);
    }

    @PostMapping
    public FinalGradeCreateResponse create(@RequestBody FinalGradeRequest finalGradeRequest) {
        return finalGradeService.create(finalGradeRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        finalGradeService.delete(id);
    }

}
