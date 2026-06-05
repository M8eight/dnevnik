package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.TeachingAssignmentDetailsProjection;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
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
    select distinct
        ta.id teachingAssignmentId,
        sc.id schoolClassId,
        sc.name schoolClassName,
        s.id subjectId,
        s.name subjectName
    from TeachingAssignment ta
    join ta.subject s
    join ta.schoolClass sc
    where ta.teacherId = :teacherId
    order by s.id asc, sc.name asc
""")
    List<TeachingAssignmentDetailsProjection> findTeachingAssignmentDetailByTeacherId(@Param("teacherId") Long teacherId);

    Optional<TeachingAssignment> findBySubjectIdAndSchoolClassIdAndTeacherId(Long subjectId, Long schoolClassId, Long teacherId);

    @Query("""
        select s.studentId
        from TeachingAssignment ta
        join ta.schoolClass sc
        join sc.students s
        where ta.id = :teachingAssignmentId
    """)
    List<Long> findStudentIdsByTeachingAssignmentId(Long teachingAssignmentId);
}
