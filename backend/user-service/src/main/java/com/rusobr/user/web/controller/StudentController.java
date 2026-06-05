package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.student.StudentService;
import com.rusobr.user.web.dto.feign.UserResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{id}/details")
    public StudentDetails getDetailsById(@PathVariable Long id) {
        return studentService.getDetailsById(id);
    }

    @PostMapping("/batch")
    public List<UserResponse> getBatch(@RequestBody List<Long> ids) {
        return studentService.getBatch(ids);
    }

    @PostMapping("/exclude-assigned")
    public List<UserResponse> getBatchWithExcludingIds(@RequestBody @NotNull Set<Long> ids) {
        return studentService.getBatchWithExcludingIds(ids);
    }

    @PatchMapping("/{studentId}/assign/{teacherId}")
    public void assignToParent(@PathVariable Long studentId,
                                      @PathVariable Long teacherId) {
        studentService.assignToParent(studentId, teacherId);
    }

    @PatchMapping("/{studentId}/unassign")
    public void unassignFromParent(@PathVariable Long studentId) {
        studentService.unassignFromParent(studentId);
    }

    @GetMapping("/{id}/with-class")
    public StudentWithClassResponse getWithClassById(@PathVariable Long id) {
        return studentService.getWithClassById(id);
    }

}
