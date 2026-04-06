package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.ScheduleService;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekItemDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/classes/{studentId}/schedule")
    public List<LessonWeekItemDto> getScheduleEndpoint( @PathVariable Long studentId, @RequestParam Long classId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        log.info("Getting schedule endpoints for class {} and student {}", classId, studentId);
        return scheduleService.getSchedule(classId, studentId, startDate, endDate);
    }

    @GetMapping("/schedule/by-date")
    public List<ScheduleLessonResponse> getScheduleByDate(@RequestParam Long studentId,
                                                          @RequestParam DayOfWeek dayOfWeek,
                                                          @RequestParam LocalDate date) {
        return scheduleService.getScheduleByDate(studentId, dayOfWeek, date);
    }
}
