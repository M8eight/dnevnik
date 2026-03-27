package com.rusobr.academic.web.dto.lessonInstance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record LessonWeekDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date,
        int lessonNumber,
        String classroom,
        String subjectName,
        Integer gradeValue,
        GradeType gradeType,
        AttendanceStatus attendanceStatus
) {}
