package com.rusobr.service.infrastructure.persistence.repository;

import com.rusobr.service.domain.model.Teacher;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends CrudRepository<Teacher, Long> {
}
