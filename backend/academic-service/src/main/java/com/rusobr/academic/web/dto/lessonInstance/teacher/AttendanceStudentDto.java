package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.domain.enums.AttendanceStatus;

public record AttendanceStudentDto(
        Long attendanceId,
        AttendanceStatus status,
        Long studentId,
        Long lessonInstanceId
) {
}
