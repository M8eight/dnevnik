package com.rusobr.academic.web.dto.scheduleLesson;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record ScheduleLessonRequest(
        @NotNull Long classId,
        @NotNull Long subjectId,
        @NotNull Long teacherId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Integer lessonNumber,
        @NotNull String classRoom,
        @NotNull LocalDate validFrom
) {
}
