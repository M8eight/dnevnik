package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.JournalService;
import com.rusobr.academic.web.dto.lessonInstance.GradesLessonsResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LessonInstanceController {

    private final JournalService lessonInstanceService;

    @GetMapping("/grades/by-student")
    public GradesLessonsResponse getGradesByStudentId(@RequestParam("studentId") Long studentId,
                                                      @RequestParam("academicPeriodId") Long academicPeriodId) {
        return lessonInstanceService.getGradesLessonsByStudentId(studentId, academicPeriodId);
    }

    @GetMapping("/journal/by-assignment")
    public TeacherJournalResponse getByAssignment(
            @RequestParam("teachingAssignmentId") Long teachingAssignmentId,
            @RequestParam("academicPeriodId") Long academicPeriodId) {
        return lessonInstanceService.getJournalByAssignment(teachingAssignmentId, academicPeriodId);
    }

    @GetMapping("/lesson-instances/by-assignment")
    public List<LessonInstanceDto> getInstanceByAssignment(@RequestParam("teachingAssignmentId") Long teachingAssignmentId,
                                                           @RequestParam("academicPeriodId") Long academicPeriodId) {
        return lessonInstanceService.getInstancesByAssignment(teachingAssignmentId, academicPeriodId);
    }

}
