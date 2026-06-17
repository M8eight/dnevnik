package com.rusobr.academic.web.dto.schoolClass;

import jakarta.validation.constraints.NotNull;

public record SchoolClassUpdateRequest(
        @NotNull String name
) {
}
