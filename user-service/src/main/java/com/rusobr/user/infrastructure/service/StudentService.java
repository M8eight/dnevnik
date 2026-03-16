package com.rusobr.user.infrastructure.service;

import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.web.dto.student.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public List<StudentResponse> findBatchStudents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return studentRepository.findAllStudentsByIds(ids);
    }

}
