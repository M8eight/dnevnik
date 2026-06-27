package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
            select
                s.id id,
                u.firstName firstName,
                u.lastName lastName,
                u.username username,
                u.keycloakId keycloakId
            from Student s join s.user u where s.id in :studentIds
                order by u.lastName
    """)
    List<UserProjection> findAllStudentsByIds(@Param("studentIds") Collection<Long> studentIds);

    @Query("""
        select
            s.id id,
            u.firstName firstName,
            u.lastName lastName,
            u.username username,
            u.keycloakId keycloakId
        from Student s join s.user u where u.id not in :studentIds
        order by u.lastName
    """)
    List<UserProjection> findAllStudentsExcludeAssigned(@Param("studentIds") Collection<Long> studentIds);

    @Query("""
        select
            s.id id,
            u.firstName firstName,
            u.lastName lastName,
            u.username username,
            u.keycloakId keycloakId
        from Student s join s.user u
        order by u.lastName
    """)
    List<UserProjection> findWithUserAllStudents();

    @EntityGraph(attributePaths = {"user"})
    Optional<Student> findWithUserById(Long userId);

    @Query(value = "select * from students where id = :id", nativeQuery = true)
    Optional<Student> findByIdWithDeleted(@Param("id") Long id);

    @Query("""
        select s
        from Student s
        left join fetch s.user su
        left join fetch s.parent p
        left join fetch p.user pu
        where s.id = :id
    """)
    Optional<Student> findStudentInfoById(@Param("id") Long id);

}
