package com.rusobr.academic.infrastructure.persistence.projection;

import java.time.LocalDate;

public interface AcademicPeriodProjection {
    Long getId();
    String getName();
    String getSchoolYear();
    Boolean getIsClosed();
    LocalDate getStartDate();
    LocalDate getEndDate();
}
