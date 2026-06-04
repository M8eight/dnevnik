package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodGradeRepository extends CrudRepository<PeriodGrade, Long> {
    @EntityGraph(attributePaths = {"academicPeriod"})
    Optional<PeriodGrade> findWithAcademicPeriodById(Long id);

    @Query("""
            select pg
            from PeriodGrade pg
            join fetch pg.academicPeriod ap
            join fetch pg.teachingAssignment ta
            join fetch ta.subject su
            where pg.studentId = :studentId
                and ap.schoolYear = :schoolYear
            order by ap.startDate asc, su.name asc
    """)
    List<PeriodGrade> findPeriodGradeByStudentId(@Param("studentId") Long studentId, @Param("schoolYear") String schoolYear);

    @Query("""
        select new com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse(
            per.id,
            per.value,
            per.description,
            per.studentId,
            ap.id
        )
        from PeriodGrade per
        join per.teachingAssignment ta
        join per.academicPeriod ap
        where ta.id = :teachingAssignmentId
            and ap.schoolYear = :schoolYear
        order by ap.id asc
""")
    List<PeriodGradeResponse> findPeriodGradesByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                                     @Param("schoolYear") String schoolYear);
}
