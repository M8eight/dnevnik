package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.student.StudentService;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/batch")
    public List<UserResponse> findBatchStudents(@RequestBody List<Long> ids) {
        return studentService.findSimpleBatchStudents(ids);
    }

    @GetMapping("/{id}")
    public StudentResponseDetail findById(@PathVariable Long id) {
        return studentService.findStudentDetailById(id);
    }

}
