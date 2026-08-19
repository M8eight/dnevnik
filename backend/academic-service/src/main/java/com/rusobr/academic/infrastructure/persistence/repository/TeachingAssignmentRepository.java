package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.TeachingAssignmentDetailsProjection;
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

    @Query("""
        select ta
        from TeachingAssignment ta
        where ta.subject.id = :subjectId
            and ta.schoolClass.id = :schoolClassId
            and ta.teacherId = :teacherId
            and (
                (:classGroupId is not null and ta.classGroup.id = :classGroupId)
                    or
                (:classGroupId is null and ta.classGroup is null)
            )
    """)
    Optional<TeachingAssignment> findBySubjectIdAndSchoolClassIdAndTeacherIdAndClassGroupId(@Param("subjectId") Long subjectId,
                                                                                            @Param("schoolClassId") Long schoolClassId,
                                                                                            @Param("teacherId") Long teacherId,
                                                                                            @Param("classGroupId") Long classGroupId);

    @Query("""
        select s.studentId
        from TeachingAssignment ta
        join ta.schoolClass sc
        join sc.students s
        where ta.id = :teachingAssignmentId
    """)
    List<Long> findStudentIdsByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId);

    @Query("""
        select ta
        from TeachingAssignment ta
        left join fetch ta.subject s
        left join fetch ta.schoolClass sc
        where ta.teacherId = :teacherId
        order by s.name
    """)
    List<TeachingAssignment> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        select count(*) > 0
        from TeachingAssignment ta
        where ta.teacherId = :teacherId
            and ta.id = :teachingAssignmentId
    """)
    boolean isTeacherOwnedAssignment(@Param("teacherId") Long teacherId, @Param("teachingAssignmentId") Long teachingAssignmentId);

    @Query("""
        select count(*) > 0
        from TeachingAssignment ta
            join ta.schoolClass sc
            join sc.students cs
        where ta.teacherId = :teacherId
        and ta.id = :teachingAssignmentId
        and cs.studentId = :studentId
    """)
    boolean isOwnedByTeacherWithStudent(@Param("teacherId") Long teacherId, @Param("teachingAssignmentId") Long teachingAssignmentId, @Param("studentId") Long studentId);

    boolean existsByClassGroupId(Long classGroupId);

}
