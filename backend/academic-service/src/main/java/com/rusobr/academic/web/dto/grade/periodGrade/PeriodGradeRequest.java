package com.rusobr.academic.web.dto.grade.periodGrade;

import java.time.LocalDate;

public record PeriodGradeRequest(
        int value,
        String description,
        Long teachingAssignmentId,
        Long studentId,
        LocalDate date
) {
}
