package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentWithSubjectProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingAssignmentRepository  extends JpaRepository<TeachingAssignment,Long> {
    @Query("select sc.id from TeachingAssignment ta join ta.schoolClass sc where ta.id = :id")
    Optional<Long> findByIdWithClassId(@Param("id") Long id);

    @Query("""
    select distinct new com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentWithSubjectProjection(
        ta.id,
        sc.id,
        sc.name,
        s.id,
        s.name
    )
    from TeachingAssignment ta
    join ta.subject s
    join ta.schoolClass sc
    where ta.teacherId = :teacherId
    order by s.id asc, sc.name asc
""")
    List<TeachingAssignmentWithSubjectProjection> findTeachingAssignmentDetailByTeacherId(@Param("teacherId") Long teacherId);
}
