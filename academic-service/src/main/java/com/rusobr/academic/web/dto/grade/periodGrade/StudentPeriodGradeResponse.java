package com.rusobr.academic.web.dto.grade.periodGrade;

public record StudentPeriodGradeResponse(
        Long studentId,
        String firstName,
        String lastName,
        Integer value,
        String description,
        Long gradeId
) {
}
