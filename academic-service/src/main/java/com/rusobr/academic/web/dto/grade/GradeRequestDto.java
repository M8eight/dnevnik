package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.enums.GradeType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GradeRequestDto (
    @NotNull Long studentId,
    @NotNull Long teacherAssignmentId,
    @NotNull LocalDate date,
    @NotNull int value,
    @NotNull GradeType gradeType
) {}