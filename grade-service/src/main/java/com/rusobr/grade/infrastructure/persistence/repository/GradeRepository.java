package com.rusobr.grade.infrastructure.persistence.repository;

import com.rusobr.grade.domain.model.Grade;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends CrudRepository<Grade, Long> {
}
