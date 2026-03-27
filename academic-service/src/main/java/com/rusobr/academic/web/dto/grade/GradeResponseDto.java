package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record GradeResponseDto(
        Long gradeId,
        Long studentId,
        int value,
        GradeType gradeType

) {}
