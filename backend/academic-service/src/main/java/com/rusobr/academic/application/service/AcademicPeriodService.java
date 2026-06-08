package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> getAll() {
        return academicPeriodRepository.findAllOrderAsc().stream()
                .map(academicPeriodMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicPeriodResponse findById(Long id) {
        return academicPeriodMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public void openPeriod(Long id) {
        AcademicPeriod period = findOrThrow(id);
        if (!period.isClosed()) {
            throw new ConflictException("Academic period is already open");
        }
        period.setClosed(false);
    }

    @Transactional
    public void closePeriod(Long id) {
        AcademicPeriod period = findOrThrow(id);
        if (period.isClosed()) {
            throw new ConflictException("Academic period is already closed");
        }
        period.setClosed(true);
    }

    @Transactional
    public AcademicPeriodResponse create(AcademicPeriodRequest request) {
        validateDates(request.startDate(), request.endDate());
        AcademicPeriod period = academicPeriodMapper.toEntity(request);
        return academicPeriodMapper.toResponse(academicPeriodRepository.save(period));
    }

    @Transactional
    public AcademicPeriodResponse update(Long id, AcademicPeriodRequest request) {
        AcademicPeriod period = findOrThrow(id);
        if (period.isClosed()) {
            throw new ConflictException("Academic period is closed");
        }

        if (request.startDate() != null) period.setStartDate(request.startDate());
        if (request.endDate() != null)   period.setEndDate(request.endDate());
        if (request.name() != null)      period.setName(request.name());
        if (request.schoolYear() != null) period.setSchoolYear(request.schoolYear());

        validateDates(period.getStartDate(), period.getEndDate());

        return academicPeriodMapper.toResponse(period);
    }

    @Transactional
    public void delete(Long id) {
        AcademicPeriod period = findOrThrow(id);
        if (!period.isClosed()) {
            throw new ConflictException("Cannot delete an open academic period");
        }
        academicPeriodRepository.delete(period);
    }

    // ─── private helpers ───────────────────────────────────────────────────────

    private AcademicPeriod findOrThrow(Long id) {
        return academicPeriodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found"));
    }

    private void validateDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ConflictException("Start date cannot be after end date");
        }
    }
}