package com.rusobr.academic.infrastructure.persistence.projection;

import com.rusobr.academic.domain.enums.AttendanceStatus;

public interface AttendanceStudentProjection {
    Long getAttendanceId();
    AttendanceStatus getStatus();
    Long getStudentId();
    Long getLessonInstanceId();
}
