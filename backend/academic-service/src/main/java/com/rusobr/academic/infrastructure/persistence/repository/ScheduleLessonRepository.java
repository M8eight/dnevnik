package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query("""
        select sl
        from ScheduleLesson sl
        join fetch sl.teachingAssignment ta
        join fetch ta.subject s
        where ta.schoolClass.id = :classId
        and sl.validFrom <= :date
        and (sl.validTo is null or sl.validTo >= :date)
        order by sl.lessonNumber
    """)
    List<ScheduleLesson> findClassSchedule(@Param("classId") Long classId, @Param("date") LocalDate date);

    @EntityGraph(attributePaths = {"teachingAssignment"})
    Optional<ScheduleLesson> findWithTeachingAssignmentById(Long id);

    @Query("""
        select count(*) > 0
        from ScheduleLesson sl
        join sl.teachingAssignment ta
        where ta.schoolClass.id = :classId
        and sl.dayOfWeek = :dayOfWeek
        and sl.lessonNumber = :lessonNumber
        and (sl.validTo is null or sl.validTo >= :validFrom)
""")
    boolean existsActiveByClassSlot(@Param("classId") Long schoolClassId,
                                    @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                    @Param("lessonNumber") Integer lessonNumber,
                                    @Param("validFrom") LocalDate validFrom);

    @Query("""
        select count(*) > 0
        from ScheduleLesson sl
        join sl.teachingAssignment ta
        where ta.id = :teachingAssignmentId
        and sl.dayOfWeek = :dayOfWeek
        and sl.lessonNumber = :lessonNumber
        and (sl.validTo is null or sl.validTo >= :validFrom)
""")
    boolean existsActiveByTeachingAssignmentSlot(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                 @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                 @Param("lessonNumber") Integer lessonNumber,
                                                 @Param("validFrom") LocalDate validFrom);

    @Query("""
        select sl
        from ScheduleLesson sl
        join sl.teachingAssignment ta
        where ta.schoolClass.id = :classId
            and sl.validFrom <= :toDate
            and (sl.validTo is null or sl.validTo >= :fromDate)
    """)
    List<ScheduleLesson> findAllByClassIdAndPeriod(@Param("classId") Long classId,
                                                   @Param("fromDate") LocalDate fromDate,
                                                   @Param("toDate") LocalDate toDate);

    @Query("""
        select sl
        from ScheduleLesson sl
        join fetch sl.teachingAssignment ta
        join fetch ta.subject su
        join ta.schoolClass sc
        join sc.students st
        where st.studentId = :studentId
        and sl.validFrom <= :endDate
        and (sl.validTo is null or sl.validTo >= :startDate)
        order by sl.lessonNumber
    """)
    List<ScheduleLesson> findDiaryScheduleByStudentId(@Param("studentId") Long studentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

}
