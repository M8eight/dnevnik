package com.rusobr.academic.web.dto.grade.createGrade;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rusobr.academic.domain.enums.GradeType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateGradeRequestDto(
        @NotNull Long studentId,
        @NotNull Long scheduleLessonId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull LocalDate date,
        @NotNull int value,
        @NotNull GradeType gradeType
) {
}