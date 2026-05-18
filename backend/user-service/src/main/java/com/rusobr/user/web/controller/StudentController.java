package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.student.StudentService;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
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
    public StudentDetails findDetailsById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @PostMapping("/batch")
    public List<UserFeignResponse> findBatchStudents(@RequestBody List<Long> ids) {
        return studentService.findSimpleBatchStudents(ids);
    }

    @PostMapping("/exclude-assigned")
    public List<UserFeignResponse> findAllStudentsExcludeAssigned(@RequestBody Set<Long> ids) {
        return studentService.getStudentsExcludingIds(ids);
    }

    @PatchMapping("/{studentId}/assign/{teacherId}")
    public void assignStudentToParent(@PathVariable Long studentId,
                                      @PathVariable Long teacherId) {
        studentService.assignStudentToParent(studentId, teacherId);
    }

    @PatchMapping("/{studentId}/unassign")
    public void assignStudentToParent(@PathVariable Long studentId) {
        studentService.unassignStudentFromParent(studentId);
    }

    @GetMapping("/{id}/with-class")
    public StudentWithClassResponse findFullById(@PathVariable @NotNull Long id) {
        return studentService.findStudentDetailById(id);
    }

}
