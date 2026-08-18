package com.rusobr.academic.web.dto.bff.student;

import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;

public record StudentInfoDto(
        Double periodAverage,
        AttendanceStudentStatus attendanceStudentStatus,
        SchoolClassResponse schoolClass
) {
}
