package com.rusobr.academic.infrastructure.persistence.projection;

public interface TeachingAssignmentDetailsProjection {
    Long getTeachingAssignmentId();
    Long getSchoolClassId();
    String getSchoolClassName();
    Long getSubjectId();
    String getSubjectName();
}
