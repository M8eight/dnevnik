package com.rusobr.academic.web.dto.grade;

import java.time.LocalDate;

public record GradeResponseDto(
        Long studentId,
        int value,
        String gradeType,
        Long gradeId,
        LocalDate date
) {
}
