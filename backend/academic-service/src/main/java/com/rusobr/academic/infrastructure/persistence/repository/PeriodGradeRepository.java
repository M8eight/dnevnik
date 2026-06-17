package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.infrastructure.persistence.projection.PeriodGradeProjection;
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
            join ap.academicYear ay
            where pg.studentId = :studentId
                and ay.id = :academicYearId
            order by ap.startDate asc, su.name asc
    """)
    List<PeriodGrade> findPeriodGradeByStudentId(@Param("studentId") Long studentId,
                                                 @Param("academicYearId") Long academicYearId);

    @Query("""
        select per
        from PeriodGrade per
        join fetch per.teachingAssignment ta
        join fetch per.academicPeriod ap
        join ap.academicYear ay
        where ta.id = :teachingAssignmentId
            and ay.id = :academicYearId
        order by ap.id asc
""")
    List<PeriodGrade> findPeriodGradesByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                             @Param("academicYearId") Long academicYearId);

}
