package com.rusobr.academic.web.dto.grade.periodGrade;

import jakarta.validation.constraints.NotNull;

public record PeriodGradeRequest(
        @NotNull Integer value,
        String description,
        @NotNull Long teachingAssignmentId,
        @NotNull Long studentId,
        @NotNull Long academicPeriodId
) {
}
