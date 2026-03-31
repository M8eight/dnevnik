package com.rusobr.academic.web.dto.grade.periodGrade;

public record PeriodGradeResponse(
    Long id,
    Integer value,
    String description,
    Long studentId
) {
}
