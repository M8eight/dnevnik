package com.rusobr.academic.web.controller;

import com.rusobr.academic.domain.service.SubjectService;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    public SubjectResponseDto createSubject(@RequestBody @Valid SubjectRequestDto createSubjectRequestDto) {
        return subjectService.createSubject(createSubjectRequestDto);
    }

    @GetMapping
    public Page<SubjectResponseDto> getSubjects(Pageable pageable) {
        return subjectService.getSubjects(pageable);
    }

    @GetMapping("/{id}")
    public SubjectResponseDto getSubjectById(@PathVariable Long id) {
        return subjectService.getSubject(id);
    }

    @PutMapping("/{id}")
    public SubjectResponseDto updateSubject(@PathVariable Long id, @RequestBody @Valid SubjectRequestDto dto) {
        return subjectService.updateSubject(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubjectById(@PathVariable Long id) {
        subjectService.deleteSubject(id);
    }
}
