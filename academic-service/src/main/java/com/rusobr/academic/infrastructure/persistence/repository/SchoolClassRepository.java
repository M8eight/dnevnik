package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    @Query("""
                select studentId
                from SchoolClass sc
                join sc.studentIds studentId
                where sc.id = :classId
            """)
    Set<Long> getStudentIdsFromSchoolClasses(@Param("classId")  Long classId);
}
