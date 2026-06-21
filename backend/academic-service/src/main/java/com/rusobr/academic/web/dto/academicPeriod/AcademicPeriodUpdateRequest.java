package com.rusobr.academic.web.dto.academicPeriod;

import java.time.LocalDate;

public record AcademicPeriodUpdateRequest(
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}
