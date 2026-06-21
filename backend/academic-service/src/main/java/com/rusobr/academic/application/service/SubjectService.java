package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.application.mapper.SubjectMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.web.dto.subject.SubjectRequest;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Transactional(readOnly = true)
    public SubjectResponseDto getById(Long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Subject not found " + id));
        return subjectMapper.toSubjectResponseDto(subject);
    }

    @Transactional(readOnly = true)
    public Page<SubjectResponseDto> getAll(Pageable pageable) {
        return subjectRepository.findAllByOrderByNameAsc(pageable).map(subjectMapper::toSubjectResponseDto);
    }

    @Transactional
    public SubjectResponseDto create(SubjectRequest dto) {
        Subject subject = subjectMapper.toSubject(dto);
        return subjectMapper.toSubjectResponseDto(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new NotFoundException("Subject not found " + id);
        }
        subjectRepository.deleteById(id);
    }

}
