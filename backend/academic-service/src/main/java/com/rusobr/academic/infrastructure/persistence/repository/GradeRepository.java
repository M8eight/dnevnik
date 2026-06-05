package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalItemProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeWithSubjectNameProjection;
import com.rusobr.academic.infrastructure.persistence.projection.StudentAverageProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    //Запрос берет из schoolClass ученика и сопоставляет оценками и ограничивает данные по teachingAssignmentId
    @Query("""
        select
            s.id studentId,
            g.id gradeId,
            g.value value,
            g.type type,
            li.lessonDate lessonDate
        from Grade g
        join g.lessonInstance li
        join li.scheduleLesson sl
        join sl.teachingAssignment ta
        join ta.schoolClass sc
        join sc.students s on s.id = g.studentId
        where ta.id = :assignmentId
        order by s.id asc, g.createdAt
    """)
    List<GradeJournalItemProjection> getClassGrades(@Param("assignmentId") Long assignmentId);

    @EntityGraph(attributePaths = {"lessonInstance"})
    Optional<Grade> findWithLessonInstanceById(Long id);

    @Query("""
            select sum(g.value * g.weight) / sum(g.weight)
            from Grade g
            join g.lessonInstance li
            where g.studentId = :studentId
            and li.lessonDate between :startDate and :endDate
            """)
    Double getAverageGrade(@Param("studentId") Long studentId,
                            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
       select
            g.id id,
            g.value value,
            g.type gradeType,
            s.name subjectName
       from Grade g
           join g.lessonInstance li
           join li.scheduleLesson sl
           join sl.teachingAssignment ta
           join ta.subject s
       where li.lessonDate = :date
       and g.studentId = :studentId
""")
    List<GradeWithSubjectNameProjection> findAllByDateAndStudentId(@Param("studentId") Long studentId, @Param("date")  LocalDate date);

    //группируем по studentId, а среднее число учитывая вес округляем до 2 знаков после запятой
    @Query(value = """
        select g.student_id as studentId,
            round((
                    sum(g.value * g.weight)::float / sum(g.weight)
            )::numeric, 2) as average
        from grades g
            join lesson_instances li on li.id = g.lesson_instance_id
            join schedule_lessons sl on sl.id = li.schedule_lesson_id
            join teaching_assignments ta on ta.id = sl.teaching_assignment_id
        where li.lesson_date between :from and :to
            and ta.id = :teachingAssignmentId
            and g.deleted_at is null
            and li.deleted_at is null
        group by g.student_id
    """, nativeQuery = true)
    List<StudentAverageProjection> findAverageStudentsByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                                           @Param("from") LocalDate from, @Param("to") LocalDate to);
}
