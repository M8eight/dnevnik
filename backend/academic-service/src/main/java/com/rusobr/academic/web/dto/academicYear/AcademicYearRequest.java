package com.rusobr.academic.web.dto.academicYear;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AcademicYearRequest(
        @NotBlank(message = "Название года не может быть пустым")
        @Size(max = 9, message = "Название года не может быть больше 9 символов")
        String name,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
