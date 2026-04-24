package com.rusobr.academic.web.dto.attendances;

import com.rusobr.academic.domain.enums.AttendanceStatus;

public record AttendanceResponse(
    Long id,
    AttendanceStatus status,
    Long studentId
) {
}
