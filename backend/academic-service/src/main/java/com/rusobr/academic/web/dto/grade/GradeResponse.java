package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.enums.GradeType;

public record GradeResponse(
        Long gradeId,
        Long studentId,
        int value,
        int weight,
        GradeType gradeType
) {}
