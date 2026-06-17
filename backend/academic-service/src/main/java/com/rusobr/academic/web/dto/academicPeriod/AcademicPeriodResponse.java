package com.rusobr.academic.web.dto.academicPeriod;


import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;

import java.time.LocalDate;

public record AcademicPeriodResponse(
        Long id,
        String name,
//        String schoolYear,
        AcademicYearResponse academicYear,
        Boolean isClosed,
        LocalDate startDate,
        LocalDate endDate
) {
}
