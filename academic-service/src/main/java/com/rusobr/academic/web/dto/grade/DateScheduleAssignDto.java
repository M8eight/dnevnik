package com.rusobr.academic.web.dto.grade;

import java.time.LocalDate;

public record DateScheduleAssignDto(
        LocalDate date,
        Long scheduleId
) {}
