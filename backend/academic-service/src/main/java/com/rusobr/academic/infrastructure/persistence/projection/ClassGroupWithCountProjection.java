package com.rusobr.academic.infrastructure.persistence.projection;

public interface ClassGroupWithCountProjection {
    Long getId();
    String getName();
    Integer getStudentCount();
}
