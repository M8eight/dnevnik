package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.web.dto.grade.GradeJournalItemDto;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
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
            select new com.rusobr.academic.web.dto.grade.GradeJournalItemDto(
                s.id,
                g.id,
                g.value,
                g.type,
                li.date
            )
            from Grade g
            join g.lessonInstance li
            join li.scheduleLesson sl
            join sl.teachingAssignment ta
            join ta.schoolClass sc
            join sc.students s on s.id = g.studentId
            where ta.id = :assignmentId
            order by s.id asc, g.createdAt
            """)
    List<GradeJournalItemDto> getClassGrades(@Param("assignmentId") Long assignmentId);

    @EntityGraph(attributePaths = {"lessonInstance"})
    Optional<Grade> findWithLessonInstanceById(Long id);

    @Query("""
            select avg(g.value * g.weight)
            from Grade g
            join g.lessonInstance li
            join li.scheduleLesson sl
            join sl.teachingAssignment ta
            join ta.schoolClass sc
            join sc.students cs
            where cs.studentId = :studentId
            and li.date between :startDate and :endDate
            """)
    Double getAverageGrade(@Param("studentId") Long studentId,
                            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
       select new com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse(
            g.id,
            g.value,
            g.type,
            s.name
       )
       from Grade g
       join g.lessonInstance li
       join li.scheduleLesson sl
       join sl.teachingAssignment ta
       join ta.subject s
       where li.date = :date
       and g.studentId = :studentId
""")
    List<GradeWithSubjectNameResponse> findAllGradesByDate(@Param("studentId") Long studentId, @Param("date")  LocalDate date);
}
