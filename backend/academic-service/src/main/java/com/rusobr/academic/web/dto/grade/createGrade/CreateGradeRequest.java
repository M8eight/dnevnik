package com.rusobr.academic.web.dto.grade.createGrade;

import com.rusobr.academic.domain.enums.GradeType;
import jakarta.validation.constraints.NotNull;

public record CreateGradeRequest(
        @NotNull Long studentId,
        @NotNull Long lessonInstanceId,
        @NotNull Long academicPeriodId,
        @NotNull Integer value,
        @NotNull Integer weight,
        @NotNull GradeType gradeType
) {
}