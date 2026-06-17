package com.rusobr.academic.web.dto.academicPeriod;

import java.time.LocalDate;

public record AcademicPeriodRequest(
        String name,
        Long academicYearId,
        LocalDate startDate,
        LocalDate endDate
) {
}
