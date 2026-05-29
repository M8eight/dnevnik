package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.homework.HomeworkDiaryResponse;

import java.time.LocalDate;
import java.util.List;

public record DiaryLessonInstanceDto(
        Long id,
        Long scheduleId,
        LocalDate lessonDate,
        List<AttendanceSimpleResponse> attendances,
        List<GradeResponse> grades,
        HomeworkDiaryResponse homework
) {
}
