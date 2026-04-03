package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.SchoolClassService;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/school-classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    @GetMapping("/{id}")
    public SchoolClassResponse getSchoolClassById(@PathVariable Long id){
        return schoolClassService.findById(id);
    }

    @GetMapping("/search/by-student")
    public SchoolClassResponse getSchoolClassByStudentId(@RequestParam("studentId") @NotNull Long studentId) {
        return schoolClassService.findClassByStudentId(studentId);
    }

    //todo допилить crud
}
