package com.rusobr.academic.infrastructure.persistence.projection;

import com.rusobr.academic.domain.enums.GradeType;

public interface GradeWithSubjectNameProjection {
    Long getId();
    Integer getValue();
    GradeType getGradeType();
    String getSubjectName();
}
