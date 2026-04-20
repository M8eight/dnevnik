package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;

import java.util.List;

public record StudentJournalDto(
        Long studentId,
        List<GradeLessonTeacherDto> grades,
        List<AttendanceLessonTeacherDto> attendances
) {

    public record GradeLessonTeacherDto(
            Long gradeId,
            Integer value,
            Integer weight,
            GradeType gradeType,
            Long lessonInstanceId
    ) {}

    public record AttendanceLessonTeacherDto(
            Long attendanceId,
            AttendanceStatus status,
            Long lessonInstanceId
    ) {}

}