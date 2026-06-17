package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.FinalGrade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FinalGradeRepository extends JpaRepository<FinalGrade, Long> {
    @Query("""
        select fg
        from FinalGrade fg
            join fetch fg.teachingAssignment ta
            join fetch ta.subject su
            join fetch fg.academicYear ay
        where fg.studentId = :studentId
            and ay.id = :academicYearId
        order by su.name asc
    """)
    List<FinalGrade> findFinalGradesByStudentId(Long studentId, Long academicYearId);

    @Query("""
        select fg
        from FinalGrade fg
            join fetch fg.teachingAssignment ta
            join fetch ta.subject su
            join fetch fg.academicYear ay
        where ta.id = :teachingAssignmentId
            and ay.id = :academicYearId
        order by su.name asc
    """)
    List<FinalGrade> findFinalGradesByTeachingAssignmentId(Long teachingAssignmentId, Long academicYearId);

    @EntityGraph(attributePaths = {"academicYear"})
    Optional<FinalGrade> findWithAcademicYearById(Long id);
}
