package com.rusobr.academic.web.dto.academicPeriod;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record AcademicPeriodDto(
        Long id,
        String name,
        String schoolYear,
        boolean isClosed,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate
) {
}
