package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.domain.enums.AttendanceStatus;

public record AttendanceStudentProjection(
        Long attendanceId,
        AttendanceStatus attendanceStatus,
        Long studentId,
        Long lessonInstanceId
) {
}
