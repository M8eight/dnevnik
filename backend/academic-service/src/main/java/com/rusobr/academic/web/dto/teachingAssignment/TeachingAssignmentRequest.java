package com.rusobr.academic.web.dto.teachingAssignment;

import jakarta.validation.constraints.NotNull;

public record TeachingAssignmentRequest(
        @NotNull Long classId,
        @NotNull Long subjectId,
        @NotNull Long teacherId
) {
}
