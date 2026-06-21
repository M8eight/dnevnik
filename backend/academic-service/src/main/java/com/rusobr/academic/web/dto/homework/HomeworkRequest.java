package com.rusobr.academic.web.dto.homework;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HomeworkRequest(
        @Size(max = 1000, message = "Текст задания не может быть больше 1000 символов")
        @Size(min = 5, message = "Текст задания не может быть меньше 5 символов")
        @NotNull String text,
        @NotNull Long lessonInstanceId
) {}
