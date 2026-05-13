package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    @Query("""
    select p
    from Parent p
    join fetch p.user u
    left join fetch p.children child
    left join fetch child.user childUser
    where p.id = :id
""")
    Optional<Parent> findWithUserById(@Param("id") Long id);

    @Query(value = "select * from parents where id = :id", nativeQuery = true)
    Optional<Parent> findByIdWithDeleted(@Param("id") Long id);
}
