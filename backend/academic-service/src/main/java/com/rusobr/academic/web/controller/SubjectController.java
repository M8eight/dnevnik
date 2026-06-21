package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.SubjectService;
import com.rusobr.academic.web.dto.subject.SubjectRequest;
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

    @GetMapping
    public Page<SubjectResponseDto> getAll(Pageable pageable) {
        return subjectService.getAll(pageable);
    }

    @PostMapping
    public SubjectResponseDto create(@RequestBody @Valid SubjectRequest createSubjectRequest) {
        return subjectService.create(createSubjectRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        subjectService.delete(id);
    }

}
