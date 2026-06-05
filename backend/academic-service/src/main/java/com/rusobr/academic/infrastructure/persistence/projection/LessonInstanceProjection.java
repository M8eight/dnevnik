package com.rusobr.academic.infrastructure.persistence.projection;

import java.time.LocalDate;

public interface LessonInstanceProjection {
    Long getId();
    LocalDate getLessonDate();
}
