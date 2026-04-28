package com.rusobr.academic.web.dto.attendances;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;

public record AttendanceResponse(
        Long attendanceId,
        Long studentId,
        AttendanceStatus status,
        LessonInstanceDto lessonInstance
) {}
