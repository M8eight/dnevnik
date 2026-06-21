package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodUpdateRequest;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;
    private final AcademicYearRepository academicYearRepository;

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> getAll() {
        return academicPeriodRepository.findAllOrderAsc().stream()
                .map(academicPeriodMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicPeriodResponse findById(Long id) {
        return academicPeriodMapper.toResponse(getWithAcademicYearOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> getAllByAcademicYear(Long academicYearId) {
        return academicPeriodRepository.findAllByAcademicYearIdOrderByStartDateAsc(academicYearId).stream()
                .map(academicPeriodMapper::toResponse)
                .toList();
    }

    @Transactional
    public void openPeriod(Long id) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        if (!academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is already open");
        }

        academicPeriod.setClosed(false);
    }

    @Transactional
    public void closePeriod(Long id) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is already closed");
        }

        academicPeriod.setClosed(true);
    }

    @Transactional
    public AcademicPeriodResponse create(AcademicPeriodRequest request) {

        validateDates(request.startDate(), request.endDate());

        AcademicYear academicYear = getAcademicYearOrThrow(request.academicYearId());
        validateAcademicYear(academicYear);

        AcademicPeriod period = academicPeriodMapper.toEntity(request);
        period.setAcademicYear(academicYear);

        return academicPeriodMapper.toResponse(academicPeriodRepository.save(period));
    }

    @Transactional
    public AcademicPeriodResponse update(Long id, AcademicPeriodUpdateRequest request) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed");
        }

        if (request.startDate() != null) academicPeriod.setStartDate(request.startDate());
        if (request.endDate() != null) academicPeriod.setEndDate(request.endDate());
        if (request.name() != null) academicPeriod.setName(request.name());

        validateDates(academicPeriod.getStartDate(), academicPeriod.getEndDate());

        return academicPeriodMapper.toResponse(academicPeriod);
    }

    @Transactional
    public void delete(Long id) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        academicPeriodRepository.delete(academicPeriod);
    }

    // helpers
    private AcademicYear getAcademicYearOrThrow(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Academic year with id " + id + " not found"));
    }

    private AcademicPeriod getWithAcademicYearOrThrow(Long id) {
        return academicPeriodRepository.findWithAcademicYearById(id)
                .orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found"));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ConflictException("Start date cannot be after end date");
        }
    }

    private void validateAcademicYear(AcademicYear academicYear) {
        if (!academicYear.getIsActive()) {
            throw new ConflictException("AcademicYear with id " + academicYear.getId() + " is not active");
        }
    }

}