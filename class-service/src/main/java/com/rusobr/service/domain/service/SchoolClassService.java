package com.rusobr.service.domain.service;

import com.rusobr.service.domain.model.Class;
import com.rusobr.service.infrastructure.persistence.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;

    public Class create(Class c) {
        return classRepository.save(c);
    }

    

}
