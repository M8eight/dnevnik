package com.rusobr.academic.web.dto.grade.finalGrade;

import jakarta.validation.constraints.NotNull;

public record FinalGradeRequest(
        @NotNull Long studentId,
        @NotNull String schoolYear,
        @NotNull Integer value,
        String description,
        @NotNull Long teachingAssignmentId
) {
}
