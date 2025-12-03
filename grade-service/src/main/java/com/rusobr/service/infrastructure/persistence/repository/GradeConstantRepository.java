package com.rusobr.service.infrastructure.persistence.repository;

import com.rusobr.service.domain.model.GradeConstant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeConstantRepository extends CrudRepository<GradeConstant, Long> {
}
