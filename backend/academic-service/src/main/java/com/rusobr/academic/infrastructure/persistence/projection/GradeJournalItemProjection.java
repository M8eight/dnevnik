package com.rusobr.academic.infrastructure.persistence.projection;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public interface GradeJournalItemProjection {
    Long getStudentId();
    Long getGradeId();
    Integer getValue();
    GradeType getType();
    LocalDate getLessonDate();
}
