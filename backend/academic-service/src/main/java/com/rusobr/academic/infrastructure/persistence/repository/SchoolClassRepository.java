package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.SchoolClass;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    @Query("""
                select s.studentId
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
            select sc
            from SchoolClass sc
                join fetch sc.students cs
                join fetch sc.academicYear
            where cs.studentId = :studentId
    """)
    Optional<SchoolClass> findSchoolClassWithClassStudentByStudentId(@Param("studentId") Long studentId);

    @Query("""
            select cs.studentId
            from ClassStudent cs
            join cs.schoolClass sc
            join TeachingAssignment ta on ta.schoolClass = sc
            where ta.id = :teachingAssignmentId
""")
    List<Long> findStudentsIdsByTeachingAssignment(@Param("teachingAssignmentId") Long teachingAssignmentId);

    List<SchoolClass> findAllByAcademicYearIdOrderByNameAsc(@Param("academicYearId") Long academicYearId);

    @EntityGraph(attributePaths = {"academicYear"})
    List<SchoolClass> findAllByOrderByNameAsc();

    boolean existsByNameAndAcademicYearId(@Param("name") String name, @Param("academicYearId") Long academicYearId);

    boolean existsByNameAndIdNot(String name, Long id);

    @EntityGraph(attributePaths = {"students"})
    Optional<SchoolClass> findWithClassStudentById(@Param("id") Long id);

    boolean existsByNameAndAcademicYearIdAndIdNot(@Param("name") String name, @Param("academicYearId") Long academicYearId, @Param("id") Long id);

    @EntityGraph(attributePaths = {"academicYear"})
    Optional<SchoolClass> findWithAcademicYearById(@Param("id") Long id);

    @Query("""
        select sc
        from SchoolClass sc
            left join fetch sc.academicYear ay
        where sc.classTeacherId = :teacherId
        order by sc.name
    """)
    List<SchoolClass> findSchoolClassesBySchoolClassTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        select distinct sc
        from SchoolClass sc
            join TeachingAssignment ta on ta.schoolClass = sc
        where ta.teacherId = :teacherId
        order by sc.name
    """)
    List<SchoolClass> findSchoolClassesTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        select cs.studentId
        from SchoolClass sc
            join sc.students cs
        where cs.studentId not in (
            select cgs.studentId
            from ClassGroupStudents cgs
            join cgs.classGroup cg
            where cg.schoolClass.id = :schoolClassId
        )
        and sc.id = :schoolClassId
    """)
    Set<Long> findUnassignedStudentIdsBySchoolClassId(@Param("schoolClassId") Long schoolClassId);

}
