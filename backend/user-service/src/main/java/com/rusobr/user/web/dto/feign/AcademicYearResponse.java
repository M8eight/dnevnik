package com.rusobr.user.web.dto.feign;

import java.time.LocalDate;

public record AcademicYearResponse(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive
) {
}
