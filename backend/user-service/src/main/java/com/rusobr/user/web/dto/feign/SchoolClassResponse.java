package com.rusobr.user.web.dto.feign;

public record SchoolClassResponse(
        Long id,
        String name,
        String year,
        Long classTeacherId
) {}
