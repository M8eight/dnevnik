package com.rusobr.academic.web.dto.grade.createGrade;

import com.rusobr.academic.domain.enums.GradeType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateGradeRequest(
        @NotNull Long studentId,
        @NotNull Long scheduleLessonId,
        @NotNull LocalDate date,
        @NotNull int value,
        @NotNull GradeType gradeType
) {
}