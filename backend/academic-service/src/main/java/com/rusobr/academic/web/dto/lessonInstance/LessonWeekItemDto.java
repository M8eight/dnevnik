package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record LessonWeekItemDto(
        LocalDate date,
        int lessonNumber,
        String classroom,
        String subjectName,
        Integer gradeValue,
        GradeType gradeType,
        AttendanceStatus attendanceStatus
) {}
