package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeachingAssignmentRepository  extends JpaRepository<TeachingAssignment,Long> {
    @Query("select sc.id from TeachingAssignment ta join ta.schoolClass sc where ta.id = :id")
    Optional<Long> findByIdWithClassId(Long id);
}
