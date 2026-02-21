package com.rusobr.academic.domain.service;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.web.dto.grade.GradeRequestDto;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;

    public Page<GradeResponseDto> getGrades(Pageable pageable) {
        return gradeRepository.findAll(pageable).map(gradeMapper::toGradeResponseDto);
    }

    public GradeResponseDto getGrade(Long id) {
        Grade grade = gradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Grade not found " + id));
        return gradeMapper.toGradeResponseDto(grade);
    }

    @Transactional
    public GradeResponseDto createGrade(GradeRequestDto grade) {
        Grade gradeEntity = gradeMapper.toGrade(grade);
        return gradeMapper.toGradeResponseDto(gradeRepository.save(gradeEntity));
    }

    @Transactional
    public GradeResponseDto updateGrade(Long id, GradeRequestDto dto) {
        Grade grade = gradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Grade not found " + id));
        gradeMapper.updateEntityFromDto(dto, grade);
        return gradeMapper.toGradeResponseDto(gradeRepository.save(grade));
    }

    @Transactional
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new NotFoundException("Grade not found " + id);
        }
        gradeRepository.deleteById(id);
    }

}
