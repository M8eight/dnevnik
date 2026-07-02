package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.SchoolClass;
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
    List<Long> findStudentIdsFromSchoolClasses(@Param("classId") Long classId);

    @Query("""
            select sc
            from SchoolClass sc
            join sc.students cs
            join fetch sc.academicYear
            where cs.studentId = :studentId
            """)
    Optional<SchoolClass> findSchoolClassByStudentId(@Param("studentId") Long studentId);

    @Query("""
            select cs.studentId
            from ClassStudent cs
            join cs.schoolClass sc
            join TeachingAssignment ta on ta.schoolClass = sc
            where ta.id = :teachingAssignmentId
""")
    List<Long> findStudentsIdsByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId);

    List<SchoolClass> findAllByAcademicYearIdOrderByNameAsc(Long academicYearId);

    @EntityGraph(attributePaths = {"academicYear"})
    List<SchoolClass> findAllByOrderByNameAsc();

    boolean existsByNameAndAcademicYearId(String name, Long academicYearId);

    boolean existsByNameAndIdNot(String name, Long id);

    @EntityGraph(attributePaths = {"students"})
    Optional<SchoolClass> findWithClassStudentById(Long id);
    boolean existsByNameAndAcademicYearIdAndIdNot(String name, Long academicYearId, Long id);

    @EntityGraph(attributePaths = {"academicYear"})
    Optional<SchoolClass> findWithAcademicYearById(Long id);

    @Query("""
        select sc
        from SchoolClass sc
        left join fetch sc.academicYear ay
        where sc.classTeacherId = :teacherId
        order by sc.name
    """)
    List<SchoolClass> findSchoolClassesByTeacherId(Long teacherId);

}
