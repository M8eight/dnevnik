package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ClassGroupStudents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ClassGroupStudentsRepository extends JpaRepository<ClassGroupStudents,Long> {

    Optional<ClassGroupStudents> findByStudentIdAndClassGroupId(Long studentId, Long classGroupId);

    @Query("""
        select cgs.studentId
        from ClassGroupStudents cgs
            join cgs.classGroup cg
        where cg.schoolClass.id = :schoolClassId
    """)
    Set<Long> findAllStudentIds(@Param("schoolClassId") Long schoolClassId);

}
