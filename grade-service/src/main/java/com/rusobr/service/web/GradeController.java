package com.rusobr.service.web;

import com.rusobr.service.domain.model.Grade;
import com.rusobr.service.domain.service.GradeService;
import com.rusobr.service.web.dto.grade.GradeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades/")
@RequiredArgsConstructor
public class GradeController {

//TODO СДЕЛАЙТ

    private final GradeService gradeService;

    @GetMapping
    public Iterable<Grade> getAllGrades() {
        return gradeService.getAllGrades();
    }

    @PostMapping
    public Grade createGrade(@RequestBody GradeRequestDto gradeRequestDto) {
        return gradeService.createGrade(gradeRequestDto);
    }

    @PutMapping
    public Grade updateGrade(@PathVariable Long id, @RequestBody Grade grade) {
        //TODO сделать здесь
        return gradeService.updateGrade(id, grade);
    }

    @DeleteMapping
    public void deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
    }

}