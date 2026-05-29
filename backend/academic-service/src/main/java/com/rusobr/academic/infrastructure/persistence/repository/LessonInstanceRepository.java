package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.web.dto.lessonInstance.GradeJournalProjection;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentProjection;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonInstanceRepository extends JpaRepository<LessonInstance, Long> {

    @Query("""
                select distinct li
                from LessonInstance li
                join fetch li.scheduleLesson sl
                join fetch sl.teachingAssignment ta
                join ta.schoolClass sc
                join sc.students st
                join fetch ta.subject su
                left join fetch Grade g on g.lessonInstance = li and g.studentId = :studentId
                left join fetch Attendance a on a.lessonInstance = li and a.studentId = :studentId
                left join fetch Homework h on h.lessonInstance = li
                where st.studentId = :studentId
                    and li.lessonDate between :startDate and :endDate
                    and (g.studentId = :studentId or g.id is null)
                    and (a.studentId = :studentId or a.id is null)
                order by li.lessonDate
            """)
    List<LessonInstance> findDiaryLessonsByStudentIdAndDateRange(@Param("studentId") Long studentId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);

    @Query("""
                select new com.rusobr.academic.web.dto.lessonInstance.GradeJournalProjection(
                    sbj.name,
                    g.id,
                    g.value,
                    g.weight,
                    g.type,
                    li.lessonDate
                )
                from LessonInstance li
                join li.scheduleLesson sl
                join sl.teachingAssignment ta
                join ta.subject sbj
                join Grade g on g.lessonInstance = li and g.studentId = :studentId
                where li.lessonDate between :startDate and :endDate
                order by li.lessonDate asc
            """)
    List<GradeJournalProjection> findGradesLessonsByStudentId(@Param("studentId") Long studentId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    @Query("""
        select distinct li.lessonDate
        from LessonInstance li
        join li.scheduleLesson sl
        join sl.teachingAssignment ta
        join ta.schoolClass sc
        join sc.students st
        where st.studentId = :studentId
            and li.lessonDate between :startDate and :endDate
        order by li.lessonDate asc
""")
    List<LocalDate> findLessonDatesByStudentId(@Param("studentId") Long studentId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("""
        select distinct new com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto(
            li.id,
            li.lessonDate
        )
        from LessonInstance li
        join li.scheduleLesson sl
        join sl.teachingAssignment ta
        where li.lessonDate between :startDate and :endDate
            and ta.id = :teachingAssignmentId
        order by li.lessonDate asc
""")
    List<LessonInstanceDto> findLessonInstanceByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                                     @Param("startDate") LocalDate startDate,
                                                                     @Param("endDate") LocalDate endDate);

    @Query("""
        select new com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentProjection(
            g.id,
            g.value,
            g.weight,
            g.type,
            g.studentId,
            li.id
        )
        from LessonInstance li
        join li.scheduleLesson sl
        join sl.teachingAssignment ta
        join li.grades g
        where li.lessonDate between :startDate and :endDate
            and ta.id = :teachingAssignmentId
        order by li.lessonDate asc
    """)
    List<GradeStudentProjection> findGradesByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    @Query("""
        select new com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentProjection(
            a.id,
            a.status,
            a.studentId,
            li.id
        )
        from LessonInstance li
        join li.scheduleLesson sl
        join sl.teachingAssignment ta
        join li.attendances a
        where li.lessonDate between :startDate and :endDate
            and ta.id = :teachingAssignmentId
        order by li.lessonDate asc
    """)
    List<AttendanceStudentProjection> findAttendancesByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                                          @Param("startDate") LocalDate startDate,
                                                                          @Param("endDate") LocalDate endDate);

    List<LessonInstance> findByScheduleLessonId(Long scheduleLessonId);

    @Modifying
    @Query("""
        update LessonInstance li
        set li.deletedAt = current_timestamp()
        where li.scheduleLesson.id = :scheduleId
            and li.lessonDate > :closeDate
            and li.deletedAt is null
            and not exists (
                select 1 from Grade g where g.lessonInstance.id = li.id and g.deletedAt is null
            )
            and not exists (
                select 1 from Attendance a where a.lessonInstance.id = li.id and a.deletedAt is null
            )
            and not exists (
                select 1 from Homework h where h.lessonInstance.id = li.id and h.deletedAt is null
            )
    """)
    void softDeleteFutureEmptyAfterDate(@Param("scheduleId") Long scheduleId, @Param("closeDate") LocalDate closeDate);

    boolean existsByScheduleLessonAndLessonDate(ScheduleLesson scheduleLesson, LocalDate lessonDate);

    @Query("""
        select distinct li
        from LessonInstance li
            join fetch li.scheduleLesson sl
            left join fetch li.attendances a
            left join fetch li.grades g
            left join fetch li.homeworks h
        where sl.id in :scheduleLessonIds
            and (a.studentId = :studentId
                or g.studentId = :studentId
                or h.id is not null)
            and li.lessonDate between :startDate and :endDate
        order by li.lessonDate asc
    """)
    List<LessonInstance> findDiaryAcademicPerformanceByStudentId(@Param("scheduleLessonIds") List<Long> scheduleLessonIds,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate,
                                                                 @Param("studentId") Long studentId);

}
