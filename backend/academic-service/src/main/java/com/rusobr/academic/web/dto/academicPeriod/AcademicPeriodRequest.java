package com.rusobr.academic.web.dto.academicPeriod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AcademicPeriodRequest(
        @NotBlank(message = "Название периода не может быть пустым")
        @Size(max = 50, message = "Название периода не может быть больше 50 символов")
        @Size(min = 4, message = "Название периода не может быть меньше 4 символов")
        String name,
        @NotNull Long academicYearId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
