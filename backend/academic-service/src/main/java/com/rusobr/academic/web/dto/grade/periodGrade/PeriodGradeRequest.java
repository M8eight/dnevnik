package com.rusobr.academic.web.dto.grade.periodGrade;

public record PeriodGradeRequest(
        Integer value,
        String description,
        Long teachingAssignmentId,
        Long studentId,
        Long academicPeriodId
) {
}
