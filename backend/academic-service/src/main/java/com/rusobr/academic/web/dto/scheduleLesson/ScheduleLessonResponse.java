package com.rusobr.academic.web.dto.scheduleLesson;

public record ScheduleLessonResponse(
        Long id,
        Integer lessonNumber,
        String subjectName,
        String classRoom
) {
}
