package com.rusobr.service.domain.service;

import com.rusobr.service.domain.model.Subject;
import com.rusobr.service.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.service.web.dto.CreateSubjectDto;
import com.rusobr.service.web.dto.UpdateSubjectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public Iterable<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject createSubject(CreateSubjectDto subjectCreateDto) {
        Subject subject = Subject.builder()
                .name(subjectCreateDto.getName())
                .teacher(subjectCreateDto.getTeacher())
                .build();

        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long id, UpdateSubjectDto updateSubjectDto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + id));

        if (updateSubjectDto.getName() != null) {
            subject.setName(updateSubjectDto.getName());
        }
        if (updateSubjectDto.getTeacher() != null) {
            subject.setTeacher(updateSubjectDto.getTeacher());
        }

        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}
