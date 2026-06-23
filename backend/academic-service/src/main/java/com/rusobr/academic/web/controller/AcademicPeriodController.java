package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.AcademicPeriodService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    @GetMapping
    public List<AcademicPeriodResponse> getAll() {
        return academicPeriodService.getAll();
    }

    @GetMapping("/{id}")
    public AcademicPeriodResponse getById(@PathVariable Long id) {
        return academicPeriodService.findById(id);
    }

    @GetMapping("/by-academic-year/{id}")
    public List<AcademicPeriodResponse> getAllByAcademicYear(@PathVariable Long id) {
        return academicPeriodService.getAllByAcademicYear(id);
    }

    @PatchMapping("/{id}/open")
    public void openPeriod(@PathVariable Long id) {
        academicPeriodService.openPeriod(id);
    }

    @PatchMapping("/{id}/close")
    public void closePeriod(@PathVariable Long id) {
        academicPeriodService.closePeriod(id);
    }

    @PostMapping
    public AcademicPeriodResponse create(@RequestBody AcademicPeriodRequest academicPeriodRequest) {
        return academicPeriodService.create(academicPeriodRequest);
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody AcademicPeriodUpdateRequest request) {
        academicPeriodService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        academicPeriodService.delete(id);
    }

}
