package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ScheduleLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<ScheduleLesson,Long> {
}
