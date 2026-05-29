package com.rusobr.academic.web.dto.scheduleLesson;

import com.rusobr.academic.web.dto.lessonInstance.DiaryLessonInstanceDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record DiaryScheduleDto(
        Long id,
        DayOfWeek dayOfWeek,
        Integer lessonNumber,
        String classRoom,
        LocalDate validFrom,
        LocalDate validTo,
        SubjectResponseDto subject,
        DiaryLessonInstanceDto instance
) {}
