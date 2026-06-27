package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeacherSubjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, TeacherSubjectId> {
    @Modifying
    @Query("""
                update TeacherSubject ts set ts.deletedAt = current_timestamp() where ts.id.subjectId = :subjectId
                and ts.id.teacherId = :teacherId
                and ts.deletedAt is null
            """)
    void softDelete(@Param("subjectId") Long subjectId, @Param("teacherId") Long teacherId);

    @Query(value = """
                select * from teacher_subjects where teacher_subjects.subject_id = :subjectId
                and teacher_subjects.teacher_id = :teacherId
            """, nativeQuery = true)
    Optional<TeacherSubject> findByIdWithDeleted(@Param("subjectId") Long subjectId,
                                                 @Param("teacherId") Long teacherId);

    @Query("""
        select ts
        from TeacherSubject ts
        left join fetch ts.subject s
        where ts.id.teacherId = :teacherId
        order by ts.subject.name
    """)
    List<TeacherSubject> findByTeacherId(@Param("teacherId") Long teacherId);

}
