package com.rusobr.academic.web.controller;

import com.rusobr.academic.domain.service.ScheduleService;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/classes/{studentId}/schedule")
    public List<LessonWeekDto> getScheduleEndpoint(@RequestParam Long classId, @PathVariable Long studentId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        log.info("Getting schedule endpoints for class {} and student {}", classId, studentId);
        return scheduleService.getSchedule(classId, studentId, startDate, endDate);
    }
}
