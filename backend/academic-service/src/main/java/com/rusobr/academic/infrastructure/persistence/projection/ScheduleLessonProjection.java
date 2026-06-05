package com.rusobr.academic.infrastructure.persistence.projection;

public interface ScheduleLessonProjection {
    Long getId();
    Integer getLessonNumber();
    String getSubjectName();
    String getClassRoom();
}
