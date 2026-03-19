package com.rusobr.academic.web.dto.academicPeriod;


import java.time.LocalDate;

public record AcademicPeriodDto(
        Long id,
        String name,
        String schoolYear,
        boolean isClosed,
        LocalDate startDate,
        LocalDate endDate
) {
}
