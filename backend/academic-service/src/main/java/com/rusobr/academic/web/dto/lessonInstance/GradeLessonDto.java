package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record GradeLessonDto(
        Long gradeId,
        Integer value,
        Integer weight,
        GradeType gradeType,
        LocalDate date
) {}
