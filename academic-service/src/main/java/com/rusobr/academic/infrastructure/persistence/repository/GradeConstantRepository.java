package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.GradeConstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeConstantRepository extends JpaRepository<GradeConstant, Long> {
}
