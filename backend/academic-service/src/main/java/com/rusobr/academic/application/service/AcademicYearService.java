package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.application.mapper.AcademicYearMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validateRequest(request);
        AcademicYear academicYear = academicYearMapper.toEntity(request);
        return academicYearMapper.toResponse(academicYearRepository.save(academicYear));
    }

    @Transactional
    public boolean isActive(Long id) {
        return findOrThrow(id).getIsActive();
    }

    @Transactional
    public void setActive(Long id, Boolean active) {
        AcademicYear academicYear = findOrThrow(id);
        if (academicYear.getIsActive().equals(active)) {
            throw new ConflictException("Academic year is already " + (active ? "active" : "inactive"));
        }
        academicYear.setIsActive(active);
    }

    @Transactional
    public AcademicYearResponse update(Long id, AcademicYearRequest request) {
        AcademicYear academicYear = findOrThrow(id);
        validateRequest(request);

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
                .orElseThrow(() -> new NotFoundException("Academic year with id " + id + " not found"));
    }

    private void validateRequest(AcademicYearRequest request) {
        if (request.startDate() != null && request.endDate() != null) {
            if (request.startDate().isAfter(request.endDate())) {
                throw new ConflictException("Start date cannot be after end date");
            }
        }
    }
}
