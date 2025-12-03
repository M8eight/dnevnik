package com.rusobr.service.domain.service;

import com.rusobr.service.domain.model.SchoolClass;
import com.rusobr.service.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.service.web.dto.SchoolClassDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository classRepository;

    public Iterable<SchoolClass> getAllSchoolClasses() {
        return classRepository.findAll();
    }

    public SchoolClass create(SchoolClassDto c) {
        SchoolClass schoolClass = SchoolClass.builder()
                .name(c.getName())
                .build();
        return classRepository.save(schoolClass);
    }

    public SchoolClass update(Long id, SchoolClass c) {
        return classRepository.save(c);
    }

    public void delete(Long id) {
        classRepository.deleteById(id);
    }

}
