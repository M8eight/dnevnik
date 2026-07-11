package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.HomeworkService;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homeworks")
public class HomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping("/by-assignment")
    public Page<HomeworkResponse> getByAssignment(@RequestParam Long teachingAssignmentId, Pageable pageable) {
        return homeworkService.getByAssignment(teachingAssignmentId, pageable);
    }

    @PostMapping
    public HomeworkResponse create(@RequestBody @Valid HomeworkRequest homeworkRequest) {
        return homeworkService.create(homeworkRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        homeworkService.delete(id);
    }

}
