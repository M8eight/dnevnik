package com.rusobr.academic.web.dto.schoolClass;

import jakarta.validation.constraints.NotNull;

public record SchoolClassRequest(
        @NotNull String name,
        @NotNull Long academicYearId
) {}
