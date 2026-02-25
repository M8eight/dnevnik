package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.domain.enums.AttendanceStatus;

import java.time.LocalDate;

public record LessonWeekDto(
        LocalDate date,
        int lessonNumber,
        String classroom,
        String subjectName,
        Integer gradeValue,
        String gradeType,
        AttendanceStatus attendanceStatus
) {}
