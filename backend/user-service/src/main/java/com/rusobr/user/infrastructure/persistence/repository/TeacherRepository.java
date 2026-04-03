package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.Teacher;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<Teacher> findWithUserById(Long id);
}
