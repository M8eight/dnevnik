package com.rusobr.academic.web.dto.classGroup;

import jakarta.validation.constraints.NotNull;

public record ClassGroupRequest(
        @NotNull String name,
        @NotNull Long schoolClassId
) {
}
