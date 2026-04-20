package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.AcademicPeriodService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
