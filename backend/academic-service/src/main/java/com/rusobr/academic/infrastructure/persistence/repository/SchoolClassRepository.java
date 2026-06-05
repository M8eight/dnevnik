package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolClassProjection;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    @Query("""
                select s.id
                from SchoolClass sc
                join sc.students s
                where sc.id = :classId
            """)
    List<Long> getStudentIdsFromSchoolClasses(@Param("classId") Long classId);

    @Query("""
            select
                 sc.id id,
                 sc.name name,
                 sc.schoolYear schoolYear,
                 sc.classTeacherId classTeacherId
            from ClassStudent cs
            join cs.schoolClass sc
            where cs.studentId = :studentId
            """)
    Optional<SchoolClassProjection> getSchoolClassByStudentId(@Param("studentId") Long studentId);

    @Query("""
            select cs.studentId
            from ClassStudent cs
            join cs.schoolClass sc
            join TeachingAssignment ta on ta.schoolClass = sc
            where ta.id = :teachingAssignmentId
""")
    List<Long> findStudentsIdsByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId);

    List<SchoolClassProjection> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @EntityGraph(attributePaths = {"students"})
    Optional<SchoolClass> findWithClassStudentById(Long id);

}
