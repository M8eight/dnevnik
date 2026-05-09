package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.service.user.UserOrchestrator;
import com.rusobr.user.infrastructure.service.user.UserService;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.user.UserRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserOrchestrator userOrchestrator;

    @GetMapping
    public Page<UserResponse> findAllByFilter(Pageable pageable,
                                              @RequestParam(required = false) UserRole role,
                                              @RequestParam(required = false) String search) {
        return userService.findAllByFilter(pageable, role, search);
    }

    @PostMapping("/students")
    public UserResponse createStudent(@RequestBody UserRequest<StudentDetails> userRequest) {
        return userOrchestrator.create(userRequest);
    }

    @PostMapping("/teachers")
    public UserResponse createTeacher(@RequestBody UserRequest<TeacherDetails> userRequest) {
        return userOrchestrator.create(userRequest);
    }

    @PostMapping("/parents")
    public UserResponse createParent(@RequestBody UserRequest<ParentDetails> userRequest) {
        return userOrchestrator.create(userRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUserCascade(id);
    }

}
