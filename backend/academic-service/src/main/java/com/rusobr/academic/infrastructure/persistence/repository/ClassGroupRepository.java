package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ClassGroup;
import com.rusobr.academic.infrastructure.persistence.projection.ClassGroupWithCountProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup,Long> {

    @Query("""
        select
            cg.id id,
            cg.name name,
            count(gs) studentCount
        from ClassGroup cg
            left join cg.classGroupStudents gs
        group by cg.id, cg.name
    """)
    List<ClassGroupWithCountProjection> findAllWithCountStudents();

    List<ClassGroup> findAllBySchoolClassId(Long schoolClassId);



    @Query("""
        select cg
        from ClassGroup cg
            left join fetch cg.classGroupStudents gs
        where cg.id = :id
    """)
    Optional<ClassGroup> findWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"classGroupStudents", "schoolClass.students"})
    Optional<ClassGroup> findWithClassGroupAndSchoolClassStudentsById(Long id);

    @EntityGraph(attributePaths = {"schoolClass"})
    Optional<ClassGroup> findWithSchoolClassById(Long id);

}
