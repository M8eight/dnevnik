package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;

import java.time.Instant;

public record GradeDetailResponse(
    Long id,
    Instant date,
    GradeType type,
    Integer value,
    Integer weight,
    UserFeignResponse teacher
) {
}
