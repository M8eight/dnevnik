package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teaching-assignments")
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    @GetMapping
    public List<TeachingAssignmentDetailsDto> getByTeacherId(@RequestParam("teacherId") @NotNull Long teacherId) {
        return teachingAssignmentService.getByTeacherId(teacherId);
    }

}
