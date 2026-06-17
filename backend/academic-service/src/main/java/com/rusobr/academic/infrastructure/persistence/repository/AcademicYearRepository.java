package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    List<AcademicYear> findAllByOrderByStartDateDesc();
}
