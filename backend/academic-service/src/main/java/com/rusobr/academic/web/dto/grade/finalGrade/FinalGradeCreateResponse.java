package com.rusobr.academic.web.dto.grade.finalGrade;

public record FinalGradeCreateResponse(
        Long id,
        Long studentId,
        String schoolYear,
        Integer value,
        String description
) {
}
