package com.rusobr.academic.domain.service;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.SubjectMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    public Page<SubjectResponseDto> getSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable).map(subjectMapper::toSubjectResponseDto);
    }

    public SubjectResponseDto getSubject(Long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Subject not found " + id));
        return subjectMapper.toSubjectResponseDto(subject);
    }

    @Transactional
    public SubjectResponseDto createSubject(SubjectRequestDto dto) {
        Subject subject = subjectMapper.toSubject(dto);

        return subjectMapper.toSubjectResponseDto(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponseDto updateSubject(Long id, SubjectRequestDto dto) {
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Subject not found " + id));

        subjectMapper.updateEntityFromDto(dto, subject);

        return subjectMapper.toSubjectResponseDto(subject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new NotFoundException("Subject not found " + id);
        }
        subjectRepository.deleteById(id);
    }
}
