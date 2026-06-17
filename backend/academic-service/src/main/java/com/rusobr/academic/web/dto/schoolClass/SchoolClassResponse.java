package com.rusobr.academic.web.dto.schoolClass;

import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;

public record SchoolClassResponse(
        Long id,
        String name,
        AcademicYearResponse academicYear,
        Long classTeacherId
) {}
