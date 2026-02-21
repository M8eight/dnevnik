package com.rusobr.academic.web.dto.gradeConstant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateGradeConstantRequestDto(

        @NotBlank(message = "Name must not be blank")
        String name,

        @NotBlank(message = "Description must not be blank")
        String description,

        @NotNull(message = "Value must not be null")
        @Positive(message = "Value must be positive")
        Integer value

) {}