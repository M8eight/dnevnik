package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.StudentService;
import com.rusobr.user.web.dto.student.StudentResponse;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/batch")
    public List<StudentResponse> findBatchStudents(@RequestBody List<Long> ids) {
        return studentService.findBatchStudents(ids);
    }

    @GetMapping("/{id}")
    public StudentResponseDetail findById(@PathVariable Long id) {
        return studentService.findStudentById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @NotNull Long id) {
        studentService.deleteStudentById(id);
    }

}
