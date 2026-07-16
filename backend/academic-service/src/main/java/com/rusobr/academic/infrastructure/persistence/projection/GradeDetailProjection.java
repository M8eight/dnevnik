package com.rusobr.academic.infrastructure.persistence.projection;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.Instant;

public interface GradeDetailProjection {
    Long getId();
    Instant getDate();
    GradeType getGradeType();
    Integer getValue();
    Integer getWeight();
    Long getTeacherId();
}
