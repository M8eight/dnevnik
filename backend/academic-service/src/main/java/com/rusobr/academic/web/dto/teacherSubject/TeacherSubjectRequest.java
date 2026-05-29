package com.rusobr.academic.web.dto.teacherSubject;

import jakarta.validation.constraints.NotNull;

public record TeacherSubjectRequest(
        @NotNull Long teacherId,
        @NotNull Long subjectId
) {
}
