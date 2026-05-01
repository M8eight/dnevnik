package com.rusobr.academic.web.dto.academicPeriod;

import java.time.LocalDate;

public record AcademicPeriodRequest(
        String name,
        String schoolYear,
        LocalDate startDate,
        LocalDate endDate
) {
}
