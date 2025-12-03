package com.rusobr.class_service.infrastructure.persistence.repository;

import com.rusobr.class_service.domain.model.GradeConstant;
import org.springframework.data.repository.CrudRepository;

public interface GradeConstantRepository extends CrudRepository<GradeConstant, Long> {
}
