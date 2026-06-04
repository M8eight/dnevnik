package com.rusobr.academic.web.dto.grade.finalGrade;

public record FinalGradeResponse(
        Long id,
        Long studentId,
        String schoolYear,
        Integer value,
        String description,
        String subjectName
) {
}
