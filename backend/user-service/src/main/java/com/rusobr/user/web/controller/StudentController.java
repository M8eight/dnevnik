package com.rusobr.user.web.controller;

import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.user.application.service.student.StudentService;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentInfoResponse;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.student.StudentWithParentDto;
import com.rusobr.user.web.dto.user.UserResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CurrentStudentContext currentStudentContext;

    @GetMapping("/{id}/details")
    public StudentDetails getDetailsById(@PathVariable Long id) {
        return studentService.getDetailsById(id);
    }

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return studentService.existsById(id);
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

    @GetMapping("/{id}/with-parent")
    public StudentWithParentDto getStudentWithParent(@PathVariable Long id) {
        return studentService.getParentByStudentId(id);
    }

    @GetMapping("/unasigned-to-parent")
    public Page<UserResponse> getUnassignedToParentStudents(Pageable pageable,
                                                            @RequestParam(required = false) String search) {
        return studentService.getUnassignedToParent(pageable, search);
    }

    @GetMapping("/by-parent")
    public List<UserResponse> getStudentsByParentId(@AuthenticationPrincipal Jwt jwt) {
        Long parentId = jwt.getClaim("user_id");
        return studentService.getStudentsByParentId(parentId);
    }

    @GetMapping("/is-child")
    public boolean isChild(@RequestParam Long parentId, @RequestParam Long studentId) {
        return studentService.isChild(parentId, studentId);
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
    public StudentWithClassResponse getWithClassById() {
        return studentService.getWithClassById(currentStudentContext.getStudentId());
    }

}
