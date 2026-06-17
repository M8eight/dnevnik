package com.rusobr.academic.web.dto.academicYear;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AcademicYearResponse(
        @NotNull Long id,
        @NotNull String name,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Boolean isActive
) {
}