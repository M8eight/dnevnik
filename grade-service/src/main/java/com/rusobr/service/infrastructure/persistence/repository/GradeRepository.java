package com.rusobr.service.infrastructure.persistence.repository;

import com.rusobr.service.domain.model.Grade;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends CrudRepository<Grade, Long> {
}
