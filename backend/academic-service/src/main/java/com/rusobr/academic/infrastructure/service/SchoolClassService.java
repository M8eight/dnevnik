package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.SchoolClassMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolClassMapper schoolClassMapper;

    public SchoolClassResponse findById(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SchoolClass Not Found by id: " + id));

        return schoolClassMapper.toSchoolClassResponse(schoolClass);
    }

    public SchoolClassResponse findClassByStudentId(Long studentId) {
        return schoolClassRepository.getSchoolClassByStudentId(studentId).orElse(null);
    }
}
