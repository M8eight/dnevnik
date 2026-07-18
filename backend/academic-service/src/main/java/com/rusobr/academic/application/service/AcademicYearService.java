package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.academic.application.mapper.AcademicYearMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final AcademicYearMapper academicYearMapper;

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAll() {
        return academicYearRepository.findAllByOrderByStartDateDesc().stream()
                .map(academicYearMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse findById(Long id) {
        return academicYearMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public AcademicYearResponse create(AcademicYearRequest request) {
        validateDates(request.startDate(), request.endDate());
        AcademicYear academicYear = academicYearMapper.toEntity(request);
        return academicYearMapper.toResponse(academicYearRepository.save(academicYear));
    }

    @Transactional
    public void open(Long id) {
        AcademicYear academicYear = findOrThrow(id);

        validateDates(academicYear.getStartDate(), academicYear.getEndDate());

        academicYear.open();
    }

    @Transactional
    public void close(Long id) {
        AcademicYear academicYear = findOrThrow(id);

        validateDates(academicYear.getStartDate(), academicYear.getEndDate());

        academicYear.close();
    }

    @Transactional
    public AcademicYearResponse update(Long id, AcademicYearRequest request) {
        AcademicYear academicYear = findOrThrow(id);
        validateDates(request.startDate(), request.endDate());

        if (request.name() != null) {
            academicYear.setName(request.name());
        }
        if (request.description() != null) {
            academicYear.setDescription(request.description());
        }
        if (request.startDate() != null) {
            academicYear.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            academicYear.setEndDate(request.endDate());
        }

        return academicYearMapper.toResponse(academicYear);
    }

    @Transactional
    public void delete(Long id) {
        AcademicYear academicYear = findOrThrow(id);
        academicYearRepository.delete(academicYear);
    }

    // helpers
    private AcademicYear findOrThrow(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Academic year with id " + id + " not found", AcademicExceptionCode.ACADEMIC_YEAR_NOT_FOUND));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new ConflictException("Start date cannot be after end date", AcademicExceptionCode.ACADEMIC_YEAR_DATES_CONFLICT);
            }
        }
    }

}
