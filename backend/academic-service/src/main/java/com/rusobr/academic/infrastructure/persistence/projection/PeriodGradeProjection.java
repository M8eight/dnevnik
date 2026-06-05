package com.rusobr.academic.infrastructure.persistence.projection;

public interface PeriodGradeProjection {
    Long getId();
    Integer getValue();
    String getDescription();
    Long getStudentId();
    Long getAcademicPeriodId();
}
