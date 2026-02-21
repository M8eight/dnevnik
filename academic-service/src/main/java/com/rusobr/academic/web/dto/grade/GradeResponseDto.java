package com.rusobr.academic.web.dto.grade;

import java.time.LocalDate;

public record GradeResponseDto(
        String student,
        String subject,
        String teacher,
        LocalDate date
) {
}
