package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeProjection;
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
                   select new com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeProjection(
                        cs.studentId,
                        pg.value,
                        pg.description,
                        pg.id
                   )
                   from TeachingAssignment ta
                   join ta.schoolClass sc
                   join sc.students cs
                   left join PeriodGrade pg on pg.studentId = cs.studentId
                               and pg.teachingAssignment = ta
                               and pg.academicPeriod.id = :academicPeriodId
                   where ta.id = :teachingAssignmentId
            """)
    List<StudentPeriodGradeProjection> findPeriodGradesByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId, @Param("academicPeriodId") Long academicPeriodId);
}
