package com.rusobr.academic.web.dto.scheduleLesson;

import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record ScheduleLessonDto(
        Long id,
        DayOfWeek dayOfWeek,
        Integer lessonNumber,
        String classRoom,
        LocalDate validFrom,
        LocalDate validTo,
        SubjectResponseDto subject,
        UserFeignResponse teacher
) {
}
