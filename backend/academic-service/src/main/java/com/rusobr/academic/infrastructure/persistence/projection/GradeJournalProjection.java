package com.rusobr.academic.infrastructure.persistence.projection;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public interface GradeJournalProjection {
    String getSubjectName();
    Long getGradeId();
    Integer getValue();
    Integer getWeight();
    GradeType getGradeType();
    LocalDate getLessonDate();
}
