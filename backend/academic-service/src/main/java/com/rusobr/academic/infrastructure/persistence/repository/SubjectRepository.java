package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject,Long> {
    Page<SubjectResponseDto> findAllByOrderByNameAsc(Pageable pageable);
}
