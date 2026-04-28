package com.rusobr.academic.web.dto.attendances.journal;

import com.rusobr.academic.domain.enums.AttendanceStatus;

public record AttendanceSimpleResponse(
    Long id,
    AttendanceStatus status,
    Long studentId
) {
}
