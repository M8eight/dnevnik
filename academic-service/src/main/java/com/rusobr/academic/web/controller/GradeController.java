package com.rusobr.academic.web.controller;

import com.rusobr.academic.domain.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeRequestDto;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {
    private final GradeService gradeService;

    @PostMapping
    public GradeResponseDto createGrade(@RequestBody @Valid GradeRequestDto createGradeRequestDto) {
        return gradeService.createGrade(createGradeRequestDto);
    }

    @GetMapping
    public Page<GradeResponseDto> getGrades(Pageable pageable) {
        return gradeService.getGrades(pageable);
    }

    @GetMapping("/{id}")
    public GradeResponseDto getGradeById(@PathVariable Long id) {
        return gradeService.getGrade(id);
    }

    @PutMapping("/{id}")
    public GradeResponseDto updateGrade(@PathVariable Long id, @RequestBody @Valid GradeRequestDto dto) {
        return gradeService.updateGrade(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteGradeById(@PathVariable Long id) {
        gradeService.deleteGrade(id);
    }
}
