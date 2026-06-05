package com.rusobr.academic.infrastructure.persistence.projection;

public interface SchoolClassProjection {
    Long getId();
    String getName();
    String getSchoolYear();
    Long getClassTeacherId();
}
