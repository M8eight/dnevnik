package com.rusobr.user.web.dto.feign;

public record SchoolClassResponse(
        Long id,
        String name,
        AcademicYearResponse academicYear,
        Long classTeacherId
) {}
