package com.rusobr.academic.web.dto.grade.finalGrade;

import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;

public record FinalGradeCreateResponse(
        Long id,
        Long studentId,
        AcademicYearResponse academicYear,
        Integer value,
        String description
) {
}
