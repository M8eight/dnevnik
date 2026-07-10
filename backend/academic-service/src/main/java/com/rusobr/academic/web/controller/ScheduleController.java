package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.ScheduleService;
import com.rusobr.academic.web.dto.scheduleLesson.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/schedules/diary")
    public List<DiaryScheduleDto> getDiaryScheduleByStudentId(@AuthenticationPrincipal Jwt jwt,
                                                              @RequestParam LocalDate startDate,
                                                              @RequestParam LocalDate endDate) {
        Long userId = jwt.getClaim("user_id");
        return scheduleService.getByStudentId(userId, startDate, endDate);
    }

    @GetMapping("/schedules/by-class")
    public Map<DayOfWeek, List<ScheduleLessonDto>> getClassSchedule(@RequestParam Long classId,
                                                                    @RequestParam LocalDate date) {
        return scheduleService.getByClass(classId, date);
    }

    @PostMapping("/schedules")
    public void create(@RequestBody ScheduleLessonRequest scheduleLessonRequest) {
        scheduleService.create(scheduleLessonRequest);
    }

    @PatchMapping("/schedules/{scheduleId}/close")
    public void close(@PathVariable Long scheduleId, @RequestParam @NotNull LocalDate closeDate) {
        scheduleService.close(scheduleId, closeDate);
    }

    @PatchMapping("/schedules/load")
    public void load(@RequestParam Long classId, @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate) {
        scheduleService.load(classId, fromDate, toDate);
    }

}
