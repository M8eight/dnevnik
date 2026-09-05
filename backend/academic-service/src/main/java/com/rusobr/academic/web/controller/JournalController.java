package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.JournalService;
import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.academic.web.dto.grade.PeriodFinalGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.GradesLessonsResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final CurrentStudentContext currentStudentContext;

    @GetMapping("/grades/by-student")
    public GradesLessonsResponse getGradesByStudentId(@RequestParam("academicPeriodId") Long academicPeriodId) {
        return journalService.getGradesByStudentId(currentStudentContext.getStudentId(), academicPeriodId);
    }

    @GetMapping("/period-final-grades/by-student")
    public List<PeriodFinalGradeResponse> getPeriodFinalGradesByStudentId(@RequestParam("academicYearId") Long academicYearId) {
        return journalService.getPeriodFinalGrades(currentStudentContext.getStudentId(), academicYearId);
    }

    @PreAuthorize("@teacherSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/journal/by-assignment")
    public TeacherJournalResponse getByAssignment(
            @RequestParam("teachingAssignmentId") Long teachingAssignmentId,
            @RequestParam("academicPeriodId") Long academicPeriodId) {
        return journalService.getJournalByAssignment(teachingAssignmentId, academicPeriodId);
    }

    @PreAuthorize("@teacherSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/lesson-instances/by-assignment")
    public List<LessonInstanceDto> getInstanceByAssignment(@RequestParam("teachingAssignmentId") Long teachingAssignmentId,
                                                           @RequestParam("academicPeriodId") Long academicPeriodId) {
        return journalService.getInstancesByAssignment(teachingAssignmentId, academicPeriodId);
    }

}
