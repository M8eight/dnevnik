package com.rusobr.academic.web.dto.grade.periodGrade;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;

public record PeriodGradeStudentResponse(
        Long id,
        Integer value,
        String description,
        String subjectName,
        AcademicPeriodResponse academicPeriod
) {
}
