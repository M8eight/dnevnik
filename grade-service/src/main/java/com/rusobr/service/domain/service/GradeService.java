package com.rusobr.service.domain.service;

import com.rusobr.service.domain.model.Grade;
import com.rusobr.service.infrastructure.mapper.GradeMapper;
import com.rusobr.service.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.service.web.dto.grade.GradeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;

    public Iterable<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    public Grade createGrade(GradeRequestDto grade) {
        Grade gradeEntity = gradeMapper.toGrade(grade);
        return gradeRepository.save(gradeEntity);
    }

    public Grade updateGrade(Long id, Grade grade) {
        //TODO сделать здесь
        return gradeRepository.save(grade);
    }

    public void deleteGrade(Long id) {
        gradeRepository.deleteById(id);
    }

}
