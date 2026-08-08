package com.rusobr.academic.web.dto.scheduleLesson.studentDiary;

import java.time.LocalDate;
import java.util.List;

public record DiaryWeekResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<DiaryDayDto> days
) {
}
