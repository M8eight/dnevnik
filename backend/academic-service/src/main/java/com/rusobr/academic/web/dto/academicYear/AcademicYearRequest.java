package com.rusobr.academic.web.dto.academicYear;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AcademicYearRequest(
        @NotNull String name,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
