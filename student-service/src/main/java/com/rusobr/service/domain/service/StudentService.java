package com.rusobr.service.domain.service;

import com.rusobr.service.domain.model.Student;
import com.rusobr.service.infrastructure.persistence.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudent(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    //todo update student

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }


}
