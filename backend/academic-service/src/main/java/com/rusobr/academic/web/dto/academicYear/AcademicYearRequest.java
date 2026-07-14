package com.rusobr.academic.web.dto.academicYear;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AcademicYearRequest(
        @NotBlank(message = "Название года не может быть пустым")
        @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Название года должно быть в формате YYYY-YYYY")
        String name,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
