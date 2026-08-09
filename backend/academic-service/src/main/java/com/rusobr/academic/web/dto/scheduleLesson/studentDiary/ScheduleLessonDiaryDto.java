package com.rusobr.academic.web.dto.scheduleLesson.studentDiary;

import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

import java.time.DayOfWeek;

public record ScheduleLessonDiaryDto(
        Long id,
        DayOfWeek dayOfWeek,
        Integer lessonNumber,
        SubjectResponseDto subject,
        String classRoom
) {
}
