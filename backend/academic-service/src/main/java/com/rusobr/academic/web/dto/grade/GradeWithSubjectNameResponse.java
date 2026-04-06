package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.enums.GradeType;

public record GradeWithSubjectNameResponse(
        Long id,
        Integer value,
        GradeType gradeType,
        String subjectName
) {
}
