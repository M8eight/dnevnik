package com.rusobr.user.web.dto.student;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;

public record StudentInfoDto(
        Double periodAverage,
        AttendanceStudentStatus attendanceStudentStatus,
        SchoolClassResponse schoolClass
) {
}
