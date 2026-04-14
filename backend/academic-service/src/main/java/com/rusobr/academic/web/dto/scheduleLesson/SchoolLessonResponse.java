package com.rusobr.academic.web.dto.scheduleLesson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;

public record SchoolLessonResponse(
        Long id,
        Integer lessonNumber,
        String subjectName,
        String classRoom,
        @JsonProperty("dayOfWeek")
        DayOfWeek dayOfWeek
) {
}
