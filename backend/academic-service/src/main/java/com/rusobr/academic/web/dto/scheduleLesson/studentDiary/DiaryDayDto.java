package com.rusobr.academic.web.dto.scheduleLesson.studentDiary;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record DiaryDayDto(
        DayOfWeek dayOfWeek,
        LocalDate date,
        List<DiaryLessonDto> lessons
) {
}
