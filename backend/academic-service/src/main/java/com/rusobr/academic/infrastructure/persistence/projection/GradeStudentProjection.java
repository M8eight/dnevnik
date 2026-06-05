package com.rusobr.academic.infrastructure.persistence.projection;

import com.rusobr.academic.domain.enums.GradeType;

public interface GradeStudentProjection {
    Long getGradeId();
    Integer getValue();
    Integer getWeight();
    GradeType getGradeType();
    Long getStudentId();
    Long getLessonInstanceId();
}
