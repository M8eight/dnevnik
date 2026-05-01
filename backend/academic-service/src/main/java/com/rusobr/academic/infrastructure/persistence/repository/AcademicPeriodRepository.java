package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod,Long> {

    @Query("select ap from AcademicPeriod ap where ap.startDate <= :date and ap.endDate >= :date ")
    Optional<AcademicPeriod> findByDate(@Param("date") LocalDate date);

    @Query("""
    select new com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse(
        ap.id,
        ap.name,
        ap.schoolYear,
        ap.closed,
        ap.startDate,
        ap.endDate
    )
    from AcademicPeriod ap
    order by ap.startDate asc
""")
    List<AcademicPeriodResponse> findAllOrderAsc();

}
