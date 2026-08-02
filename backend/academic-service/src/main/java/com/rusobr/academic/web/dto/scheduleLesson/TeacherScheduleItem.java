package com.rusobr.academic.web.dto.scheduleLesson;

import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

import java.time.DayOfWeek;

public record TeacherScheduleItem(
        LessonInstanceDto lessonInstance,
        SubjectResponseDto subject,
        ScheduleLessonResponse scheduleLesson,
        SchoolClassResponse schoolClass,
        DayOfWeek dayOfWeek
) {
}
