package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
}
