package com.rusobr.academic.web.dto.schoolClass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SchoolClassRequest(
        @NotBlank
        @Size(max = 20, message = "Название класса не может быть больше 20 символов")
        String name,
        @NotNull Long academicYearId
) {}
