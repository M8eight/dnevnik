package com.rusobr.class_service.infrastructure.persistence.repository;

import com.rusobr.class_service.domain.model.Subject;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends CrudRepository<Subject, Long> {
}
