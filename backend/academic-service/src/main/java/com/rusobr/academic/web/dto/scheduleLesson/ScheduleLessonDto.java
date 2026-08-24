package com.rusobr.academic.web.dto.scheduleLesson;

import com.rusobr.academic.web.dto.classGroup.ClassGroupResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.common.dto.UserFeignResponse;

import java.time.DayOfWeek;

public record ScheduleLessonDto(
        Long id,
        DayOfWeek dayOfWeek,
        Integer lessonNumber,
        String classRoom,
        SubjectResponseDto subject,
        ClassGroupResponse classGroup,
        UserFeignResponse teacher
) {
}
