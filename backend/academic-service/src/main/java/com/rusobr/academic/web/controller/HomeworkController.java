package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.HomeworkService;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homeworks")
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PreAuthorize("@teacherSecurity.canViewAssignment(#teachingAssignmentId, authentication)")
    @GetMapping("/by-assignment")
    public Page<HomeworkResponse> getByAssignment(@RequestParam Long teachingAssignmentId, Pageable pageable) {
        return homeworkService.getByAssignment(teachingAssignmentId, pageable);
    }

    @PreAuthorize("@teacherSecurity.canCreateHomework(#homeworkRequest.lessonInstanceId(), authentication)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HomeworkResponse create(@RequestBody @Valid HomeworkRequest homeworkRequest) {
        return homeworkService.create(homeworkRequest);
    }

    @PreAuthorize("@teacherSecurity.canDeleteHomework(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        homeworkService.delete(id);
    }

}
