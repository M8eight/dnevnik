package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ScheduleLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ScheduleLessonRepository extends JpaRepository<ScheduleLesson, Long> {
    @Query("select sl.dayOfWeek from ScheduleLesson sl where sl.teachingAssignment.id = :teachingAssignmentId")
    List<DayOfWeek> findDaysOfWeeksByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId);

    List<ScheduleLesson> findByTeachingAssignmentId(Long teachingAssignmentId);
}
