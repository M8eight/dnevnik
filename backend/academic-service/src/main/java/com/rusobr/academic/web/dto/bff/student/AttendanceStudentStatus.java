package com.rusobr.academic.web.dto.bff.student;

public record AttendanceStudentStatus(
        Double presencePercent,
        Integer lateCount,
        Integer absenceCount,
        Integer lessonsCount
) {
}
