package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequestDto;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/{id}")
    public GradeResponseDto getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id);
    }

    @PostMapping
    public CreateGradeResponseDto createGrade(@RequestBody @Valid CreateGradeRequestDto createGradeRequestDto) {
        return gradeService.createGrade(createGradeRequestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteGradeById(@PathVariable Long id) {
        gradeService.deleteGrade(id);
    }
}
