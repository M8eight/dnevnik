package com.rusobr.academic.web.dto.academicPeriod;

import java.time.LocalDate;

public record AcademicPeriodSetDateRequest(
        LocalDate startDate,
        LocalDate endDate,
        String name,
        String schoolName
) {
}
