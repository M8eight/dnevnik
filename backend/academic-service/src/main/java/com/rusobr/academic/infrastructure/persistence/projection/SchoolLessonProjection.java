package com.rusobr.academic.infrastructure.persistence.projection;

import java.time.DayOfWeek;

public interface SchoolLessonProjection {
    Long getId();
    Integer getLessonNumber();
    String getSubjectName();
    String getClassRoom();
    DayOfWeek getDayOfWeek();
}
