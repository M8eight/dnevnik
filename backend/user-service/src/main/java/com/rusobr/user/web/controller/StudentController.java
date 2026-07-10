package com.rusobr.user.web.controller;

import com.rusobr.user.application.service.student.StudentService;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentInfoResponse;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @GetMapping("/{id}/info")
    public StudentInfoResponse getStudentInfoById(@PathVariable Long id) {
        return studentService.getStudentInfoById(id);
    }

    @PostMapping("/batch")
    public BatchUserResponse getBatch(@RequestBody List<Long> ids) {
        return studentService.getBatch(ids);
    }

    @PostMapping("/exclude-assigned")
    public List<UserFeignResponse> getBatchWithExcludingIds(@RequestBody @NotNull Set<Long> ids) {
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

    @GetMapping("/with-class")
    public StudentWithClassResponse getWithClassById(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return studentService.getWithClassById(userId);
    }

}
