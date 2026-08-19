package com.rusobr.academic.web.dto.classGroup;

public record ClassGroupWithCountResponse(
        Long id,
        String name,
        Integer studentCount
) {
}
