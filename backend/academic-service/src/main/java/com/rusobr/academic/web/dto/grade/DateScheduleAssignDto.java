package com.rusobr.academic.web.dto.grade;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record DateScheduleAssignDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date,
        Long scheduleId
) {}
