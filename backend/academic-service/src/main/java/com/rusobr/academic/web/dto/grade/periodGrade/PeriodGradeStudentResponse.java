package com.rusobr.academic.web.dto.grade.periodGrade;

public record PeriodGradeStudentResponse(
        Long id,
        Integer value,
        String description,
        String subjectName,
        Long academicPeriodId
) {
}
