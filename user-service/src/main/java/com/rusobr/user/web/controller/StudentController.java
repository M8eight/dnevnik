package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.StudentService;
import com.rusobr.user.web.dto.student.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
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

}
