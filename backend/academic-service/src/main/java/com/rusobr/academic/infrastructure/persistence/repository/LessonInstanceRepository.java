package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.lessonInstance.GradeJournalProjection;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentProjection;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    Optional<LessonInstance> findByLessonDateAndScheduleLessonId(LocalDate date, Long scheduleLessonId);

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
        join TeachingAssignment ta on ta.id = :teachingAssignmentId
        where li.lessonDate between :startDate and :endDate
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
}
