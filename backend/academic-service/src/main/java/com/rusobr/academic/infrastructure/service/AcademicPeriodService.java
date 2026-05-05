package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AcademicPeriodService {
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;

    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> getAcademicPeriods() {
        return academicPeriodRepository.findAllOrderAsc();
    }

    @Transactional
    public void closePeriod(Long id) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(id).orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found"));
        academicPeriod.setClosed(true);
    }

    @Transactional
    public void openPeriod(Long id) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(id).orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found"));
        academicPeriod.setClosed(false);
    }

    @Transactional
    public AcademicPeriodResponse createAcademicPeriod(AcademicPeriodRequest academicPeriodRequest) {
        AcademicPeriod academicPeriod = academicPeriodMapper.toEntity(academicPeriodRequest);
        return academicPeriodMapper.toDto(academicPeriodRepository.save(academicPeriod));
    }

    @Transactional
    public void setDateById(Long id, AcademicPeriodResponse req) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(id).orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found"));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed");
        }

        // 1. Сначала применяем изменения
        if (req.startDate() != null) academicPeriod.setStartDate(req.startDate());
        if (req.endDate() != null) academicPeriod.setEndDate(req.endDate());
        if (req.name() != null) academicPeriod.setName(req.name());
        if (req.schoolYear() != null) academicPeriod.setSchoolYear(req.schoolYear());

        // 2. А теперь проверяем ИТОГОВЫЙ результат в объекте
        if (academicPeriod.getStartDate() != null && academicPeriod.getEndDate() != null) {
            if (academicPeriod.getStartDate().isAfter(academicPeriod.getEndDate())) {
                throw new ConflictException("Final start date cannot be after end date");
            }
        }
    }

    @Transactional(readOnly = true)
    public AcademicPeriodResponse findById(Long id) {
        return academicPeriodMapper.toDto(academicPeriodRepository.findById(id).orElseThrow(() -> new NotFoundException("Academic period with id " + id + " not found!")));
    }

    @Transactional
    public void deleteById(Long id) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(id).orElseThrow(() -> new NotFoundException("Academic Period not found"));
        academicPeriodRepository.delete(academicPeriod);
    }
}
