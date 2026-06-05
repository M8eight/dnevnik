package com.rusobr.academic.web.dto.schoolClass;

public record SchoolClassResponse(
        Long id,
        String name,
        String schoolYear,
        Long classTeacherId
) {}
