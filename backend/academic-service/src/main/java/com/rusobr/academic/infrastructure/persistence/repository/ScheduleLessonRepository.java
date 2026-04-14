package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleLessonRepository extends JpaRepository<ScheduleLesson, Long> {
    @Query("select sl.dayOfWeek from ScheduleLesson sl where sl.teachingAssignment.id = :teachingAssignmentId")
    List<DayOfWeek> findDaysOfWeeksByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId);

    @Query("""
        select new com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse(
            sl.id,
            sl.lessonNumber,
            su.name,
            sl.classRoom
        )
        from ScheduleLesson sl
        join sl.teachingAssignment ta
        join ta.schoolClass sc
        join sc.students st
        join ta.subject su
        where st.studentId = :studentId
        and sl.dayOfWeek = :dayOfWeek
        and sl.validFrom <= :date
        and (sl.validTo is null or sl.validTo >= :date)
""")
    List<ScheduleLessonResponse> getScheduleByDate(@Param("studentId") Long studentId,
                                                   @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                   @Param("date") LocalDate date);

    @Query("""
            select new com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse(
                sl.id,
                sl.lessonNumber,
                su.name,
                sl.classRoom,
                sl.dayOfWeek
            )
            from ScheduleLesson sl
            join sl.teachingAssignment ta
            join ta.schoolClass sc
            join sc.students st
            join ta.subject su
            where st.studentId = :studentId
            """)
    List<SchoolLessonResponse> findAllByStudentId(@Param("studentId") Long studentId);

    List<ScheduleLesson> findByTeachingAssignmentId(Long teachingAssignmentId);
}
