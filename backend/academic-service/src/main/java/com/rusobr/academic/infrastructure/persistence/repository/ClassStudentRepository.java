package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.ClassStudent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ClassStudentRepository extends CrudRepository<ClassStudent, Long> {
    Optional<ClassStudent> findByStudentId(Long studentId);
    boolean existsByStudentId(Long studentId);
    Optional<ClassStudent> findBySchoolClassIdAndStudentId(Long classId, Long studentId);
    @Query("select cs.studentId from ClassStudent cs")
    Set<Long> findAllStudentIds();
}
