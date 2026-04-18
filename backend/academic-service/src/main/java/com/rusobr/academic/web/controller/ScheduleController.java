package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.ScheduleService;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedule/by-date")
    public List<ScheduleLessonResponse> getScheduleByDate(@RequestParam Long studentId,
                                                          @RequestParam DayOfWeek dayOfWeek,
                                                          @RequestParam LocalDate date) {
        return scheduleService.getScheduleByDate(studentId, dayOfWeek, date);
    }

    @GetMapping("/schedules/by-student")
    public Map<DayOfWeek, List<SchoolLessonResponse>> getWeekSchedule(@RequestParam Long studentId) {
        return scheduleService.getWeekSchedule(studentId);
    }
}
