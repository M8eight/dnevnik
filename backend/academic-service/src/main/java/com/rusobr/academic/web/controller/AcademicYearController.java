package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.AcademicYearService;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicYearResponse create(@RequestBody @Valid AcademicYearRequest academicYearRequest) {
        return academicYearService.create(academicYearRequest);
    }

    @PatchMapping("/{id}/open")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void openPeriod(@PathVariable Long id) {
        academicYearService.open(id);
    }

    @PatchMapping("/{id}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closePeriod(@PathVariable Long id) {
        academicYearService.close(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody @Valid AcademicYearRequest request) {
        academicYearService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        academicYearService.delete(id);
    }

}
