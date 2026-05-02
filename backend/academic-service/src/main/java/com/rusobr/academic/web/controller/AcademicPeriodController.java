package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.AcademicPeriodService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    @GetMapping("/{id}")
    public AcademicPeriodResponse getAcademicPeriodById(@PathVariable("id") Long id){
        return academicPeriodService.findById(id);
    }

    @GetMapping
    public List<AcademicPeriodResponse> getAcademicPeriods() {
        return academicPeriodService.getAcademicPeriods();
    }

    @PostMapping("/{id}/open")
    public void openPeriod(@PathVariable("id") Long id) {
        academicPeriodService.openPeriod(id);
    }

    @PostMapping("/{id}/close")
    public void closePeriod(@PathVariable("id") Long id) {
        academicPeriodService.closePeriod(id);
    }

    @PostMapping
    public AcademicPeriodResponse createAcademicPeriod(@RequestBody AcademicPeriodRequest academicPeriodRequest) {
        return academicPeriodService.createAcademicPeriod(academicPeriodRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteAcademicPeriod(@PathVariable("id") Long id) {
        academicPeriodService.deleteById(id);
    }

}
