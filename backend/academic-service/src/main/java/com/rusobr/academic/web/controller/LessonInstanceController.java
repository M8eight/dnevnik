package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.LessonInstanceService;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryLessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LessonInstanceController {

    private final LessonInstanceService lessonInstanceService;

    @GetMapping("/schedule")
    public Map<LocalDate, List<DiaryLessonResponse>> getScheduleByStudentIdAndDatePeriod(@RequestParam("studentId") Long studentId,
                                                                                         @RequestParam("startDate") LocalDate startDate,
                                                                                         @RequestParam("endDate") LocalDate endDate) {
        return lessonInstanceService.getDiaryLessonsByStudentIdAndDateRange(studentId, startDate, endDate);
    }

}
