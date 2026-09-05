package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.ScheduleService;
import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonDetails;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonRequest;
import com.rusobr.academic.web.dto.scheduleLesson.TeacherScheduleItem;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryWeekResponse;
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
@RequestMapping("/api/v1")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final CurrentStudentContext currentStudentContext;

    @GetMapping("/schedules/diary")
    public DiaryWeekResponse getDiaryScheduleByStudentId(@RequestParam LocalDate startDate,
                                                         @RequestParam LocalDate endDate) {
        return scheduleService.getByStudentId(currentStudentContext.getStudentId(), startDate, endDate);
    }

    @GetMapping("/schedules/by-class")
    public Map<DayOfWeek, Map<Integer, List<ScheduleLessonDto>>> getClassSchedule(@RequestParam Long classId,
                                                                    @RequestParam LocalDate date) {
        return scheduleService.getByClass(classId, date);
    }

    @GetMapping("/schedules/by-teacher/date")
    public List<TeacherScheduleItem> getTeacherScheduleDate(@AuthenticationPrincipal Jwt jwt, @RequestParam LocalDate date) {
        Long teacherId = jwt.getClaim("user_id");
        return scheduleService.getByTeacherIdDate(teacherId, date);
    }

    @GetMapping("/schedules/by-teacher/period")
    public Map<DayOfWeek, List<TeacherScheduleItem>> getTeacherSchedulePeriod(@AuthenticationPrincipal Jwt jwt,
                                                                              @RequestParam LocalDate startDate,
                                                                              @RequestParam LocalDate endDate) {
        Long teacherId = jwt.getClaim("user_id");
        return scheduleService.getByTeacherIdPeriod(teacherId, startDate, endDate);
    }

    @GetMapping("/schedules/{id}/details")
    public ScheduleLessonDetails getScheduleDetails(@PathVariable("id") Long id) {
        return scheduleService.getDetails(id);
    }

    @PostMapping("/schedules")
    public void create(@RequestBody ScheduleLessonRequest scheduleLessonRequest) {
        scheduleService.create(scheduleLessonRequest);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public void delete(@PathVariable Long scheduleId) {
        scheduleService.delete(scheduleId);
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
