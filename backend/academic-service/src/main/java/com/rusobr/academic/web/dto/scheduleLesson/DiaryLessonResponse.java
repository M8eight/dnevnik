package com.rusobr.academic.web.dto.scheduleLesson;

import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record DiaryLessonResponse(
    LocalDate lessonDate,
    String subjectName,
    DayOfWeek dayOfWeek,
    Integer lessonNumber,
    String classRoom,
    List<HomeworkResponse> homeworks,
    List<GradeResponse> grades,
    AttendanceResponse attendance
) {
}
