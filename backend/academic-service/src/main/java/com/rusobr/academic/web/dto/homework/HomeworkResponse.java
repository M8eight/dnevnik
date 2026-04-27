package com.rusobr.academic.web.dto.homework;

import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;

public record HomeworkResponse(
    Long id,
    String text,
    LessonInstanceDto lessonInstance
) {
}
