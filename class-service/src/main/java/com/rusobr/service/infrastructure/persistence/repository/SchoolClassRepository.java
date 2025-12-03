package com.rusobr.service.infrastructure.persistence.repository;

import com.rusobr.service.domain.model.Class;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends CrudRepository<Class, Long> {
}
