package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.student.StudentService;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/batch")
    public List<UserFeignResponse> findBatchStudents(@RequestBody List<Long> ids) {
        return studentService.findSimpleBatchStudents(ids);
    }

    @PatchMapping("/{studentId}/assign/{teacherId}")
    public void assignStudentToParent(@PathVariable(name = "studentId") Long studentId,
                                      @PathVariable(name = "teacherId") Long teacherId) {
        studentService.assignStudentToParent(studentId, teacherId);
    }

    @PatchMapping("/{studentId}/unassign")
    public void assignStudentToParent(@PathVariable(name = "studentId") Long studentId) {
        studentService.unassignStudentFromParent(studentId);
    }

    @GetMapping("/{id}")
    public StudentResponseDetail findById(@PathVariable Long id) {
        return studentService.findStudentDetailById(id);
    }

}
