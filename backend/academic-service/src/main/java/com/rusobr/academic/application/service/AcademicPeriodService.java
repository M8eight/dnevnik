package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodUpdateRequest;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    public AcademicPeriod getById(Long id) {
        return academicPeriodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Academic period with id %d not found".formatted(id),
                        AcademicExceptionCode.ACADEMIC_PERIOD_NOT_FOUND));
    }

    @Cacheable(value = "academicPeriods", key = "#date")
    public AcademicPeriod getByDate(LocalDate date) {
        return academicPeriodRepository.findByDate(date)
                .orElseThrow(() -> new NotFoundException("Academic period by date %s not found".formatted(date),
                        AcademicExceptionCode.ACADEMIC_PERIOD_NOT_FOUND));
    }

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

    @Cacheable(value = "academicPeriods", key = "#academicYearId")
    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> getAllByAcademicYear(Long academicYearId) {
        return academicPeriodRepository.findAllByAcademicYearIdOrderByStartDateAsc(academicYearId).stream()
                .map(academicPeriodMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = "academicPeriods", allEntries = true)
    @Transactional
    public void openPeriod(Long id) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        academicPeriod.open();
        log.info("Academic period opened: id={}, academicYearId={}", id, academicPeriod.getAcademicYear().getId());
    }

    @CacheEvict(value = "academicPeriods", allEntries = true)
    @Transactional
    public void closePeriod(Long id) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        academicPeriod.close();
        log.info("Academic period closed: id={}, academicYearId={}", id, academicPeriod.getAcademicYear().getId());
    }

    @CacheEvict(value = "academicPeriods", allEntries = true)
    @Transactional
    public AcademicPeriodResponse create(AcademicPeriodRequest request) {
        validateDates(request.startDate(), request.endDate());

        AcademicYear academicYear = getAcademicYearOrThrow(request.academicYearId());
        validateAcademicYear(academicYear);

        AcademicPeriod period = academicPeriodMapper.toEntity(request);
        period.setAcademicYear(academicYear);

        AcademicPeriodResponse response = academicPeriodMapper.toResponse(academicPeriodRepository.save(period));
        log.info("Academic period created: request={}", request);
        return response;
    }

    @CacheEvict(value = "academicPeriods", allEntries = true)
    @Transactional
    public void update(Long id, AcademicPeriodUpdateRequest request) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is already closed", AcademicExceptionCode.ACADEMIC_PERIOD_CLOSE_CONFLICT);
        }

        if (academicPeriodRepository.existsByName(request.name())) {
            throw new ConflictException("Academic period with name " + request.name() + " already exists"
            , AcademicExceptionCode.ACADEMIC_PERIOD_ALREADY_EXISTS);
        }

        if (request.name() != null) academicPeriod.setName(request.name());
        log.info("Academic period updated: id={} request={}", id, request);
    }

    @CacheEvict(value = "academicPeriods", allEntries = true)
    @Transactional
    public void delete(Long id) {
        AcademicPeriod academicPeriod = getWithAcademicYearOrThrow(id);

        validateAcademicYear(academicPeriod.getAcademicYear());

        academicPeriodRepository.delete(academicPeriod);
        log.info("Academic period deleted: id={}", id);
    }

    // helpers
    private AcademicYear getAcademicYearOrThrow(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Academic year with id " + id + " not found"
                , AcademicExceptionCode.ACADEMIC_YEAR_NOT_FOUND));
    }

    private AcademicPeriod getWithAcademicYearOrThrow(Long id) {
        return academicPeriodRepository.findWithAcademicYearById(id)
                .orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found"
                , AcademicExceptionCode.ACADEMIC_PERIOD_NOT_FOUND));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new ConflictException("Start date must be before end date", AcademicExceptionCode.ACADEMIC_PERIOD_DATES_CONFLICT);
        }
    }

    private void validateAcademicYear(AcademicYear academicYear) {
        if (academicYear.isClosed()) {
            throw new ConflictException("Academic year with id " + academicYear.getId() + " is closed"
            , AcademicExceptionCode.ACADEMIC_YEAR_CLOSED_CONFLICT);
        }
    }

}