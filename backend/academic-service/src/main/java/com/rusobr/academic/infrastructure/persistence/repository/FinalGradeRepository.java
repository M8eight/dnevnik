package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.FinalGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FinalGradeRepository extends JpaRepository<FinalGrade, Long> {
    @Query("""
        select fg
        from FinalGrade fg
            join fetch fg.teachingAssignment ta
            join fetch ta.subject su
        where fg.studentId = :studentId
            and fg.schoolYear = :schoolYear
        order by su.name asc
    """)
    List<FinalGrade> findFinalGradesByStudentId(Long studentId, String schoolYear);

    @Query("""
        select fg
        from FinalGrade fg
            join fetch fg.teachingAssignment ta
            join fetch ta.subject su
        where ta.id = :teachingAssignmentId
            and fg.schoolYear = :schoolYear
        order by su.name asc
    """)
    List<FinalGrade> findFinalGradesByTeachingAssignmentId(Long teachingAssignmentId, String schoolYear);
}
