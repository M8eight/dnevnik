package com.rusobr.user.web.dto.student;

public record AttendanceStudentStatus(
        Double presencePercent,
        Integer lateCount,
        Integer absenceCount,
        Integer lessonsCount
) {
}
