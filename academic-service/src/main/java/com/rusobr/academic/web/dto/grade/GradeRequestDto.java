package com.rusobr.academic.web.dto.grade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GradeRequestDto (
    @NotBlank String student,
    @NotBlank String subject,
    @NotBlank String teacher,
    @NotNull LocalDate date
) {}