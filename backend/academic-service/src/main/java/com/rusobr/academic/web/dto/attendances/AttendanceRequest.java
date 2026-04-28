package com.rusobr.academic.web.dto.attendances;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull Long studentId,
        @NotNull AttendanceStatus status,
        @NotNull Long lessonInstanceId
) {}
