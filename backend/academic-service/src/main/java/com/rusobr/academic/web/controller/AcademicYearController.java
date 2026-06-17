package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.AcademicYearService;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping
    public List<AcademicYearResponse> getAll() {
        return academicYearService.getAll();
    }

    @GetMapping("/{id}")
    public AcademicYearResponse getById(@PathVariable Long id){
        return academicYearService.findById(id);
    }

    @PostMapping
    public AcademicYearResponse create(@RequestBody AcademicYearRequest academicYearRequest) {
        return academicYearService.create(academicYearRequest);
    }

    @PatchMapping("/active/{id}")
    public void setActive(@PathVariable Long id, @RequestParam Boolean active) {
        academicYearService.setActive(id, active);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody AcademicYearRequest request) {
        academicYearService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        academicYearService.delete(id);
    }

}
