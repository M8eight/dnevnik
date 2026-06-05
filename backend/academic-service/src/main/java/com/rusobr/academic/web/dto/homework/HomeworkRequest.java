package com.rusobr.academic.web.dto.homework;

import jakarta.validation.constraints.NotNull;

public record HomeworkRequest(
        @NotNull String text,
        @NotNull Long lessonInstanceId
) {}
