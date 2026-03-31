package com.rusobr.academic.web.dto.grade.periodGrade;

public record StudentPeriodGradeProjection(
        Long studentId,
        Integer value,
        String description,
        Long gradeId
) {
}
