package com.rusobr.academic.web.dto.lessonInstance;

import java.time.LocalDate;

public record LessonInstanceDto(
        Long id,
        LocalDate date
) {
}
