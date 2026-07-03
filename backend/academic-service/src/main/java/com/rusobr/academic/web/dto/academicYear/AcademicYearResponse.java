package com.rusobr.academic.web.dto.academicYear;

import java.time.LocalDate;

public record AcademicYearResponse(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Boolean closed
) {
}