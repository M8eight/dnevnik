package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.web.dto.grade.PeriodFinalGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.GradesLessonsResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @GetMapping("/grades/by-student")
    public GradesLessonsResponse getGradesByStudentId(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam("academicPeriodId") Long academicPeriodId) {
        Long userId = jwt.getClaim("user_id");
        return journalService.getGradesByStudentId(userId, academicPeriodId);
    }

    @GetMapping("/period-final-grades/by-student")
    public List<PeriodFinalGradeResponse> getPeriodFinalGradesByStudentId(@AuthenticationPrincipal Jwt jwt,
                                                                          @RequestParam("academicYearId") Long academicYearId) {
        Long userId = jwt.getClaim("user_id");
        return journalService.getPeriodFinalGrades(userId, academicYearId);
    }

    @PreAuthorize("@gradeSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/journal/by-assignment")
    public TeacherJournalResponse getByAssignment(
            @RequestParam("teachingAssignmentId") Long teachingAssignmentId,
            @RequestParam("academicPeriodId") Long academicPeriodId) {
        return journalService.getJournalByAssignment(teachingAssignmentId, academicPeriodId);
    }

    @PreAuthorize("@gradeSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/lesson-instances/by-assignment")
    public List<LessonInstanceDto> getInstanceByAssignment(@RequestParam("teachingAssignmentId") Long teachingAssignmentId,
                                                           @RequestParam("academicPeriodId") Long academicPeriodId) {
        return journalService.getInstancesByAssignment(teachingAssignmentId, academicPeriodId);
    }

}
