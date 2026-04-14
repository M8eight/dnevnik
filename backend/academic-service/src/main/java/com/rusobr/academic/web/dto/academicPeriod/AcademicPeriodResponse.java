package com.rusobr.academic.web.dto.academicPeriod;


import java.time.LocalDate;

public record AcademicPeriodResponse(
        Long id,
        String name,
        String schoolYear,
        boolean isClosed,
        LocalDate startDate,
        LocalDate endDate
) {
}
