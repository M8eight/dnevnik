package com.rusobr.academic.web.dto.scheduleLesson;

import com.rusobr.academic.web.dto.classGroup.ClassGroupResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.common.dto.UserFeignResponse;

import java.time.LocalDate;

public record ScheduleLessonDetails(
        Long id,
        String classRoom,
        SubjectResponseDto subject,
        ClassGroupResponse classGroup,
        SchoolClassResponse schoolClass,
        LocalDate validFrom,
        LocalDate validTo,
        UserFeignResponse teacher
) {
}
