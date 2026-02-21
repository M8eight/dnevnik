package com.rusobr.academic.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SchoolClassResponseDto(

        Long id,

        @NotBlank
        @Size(max = 50)
        String name

) {}