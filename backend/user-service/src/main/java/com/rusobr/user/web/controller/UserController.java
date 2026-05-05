package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.user.UserOrchestrator;
import com.rusobr.user.infrastructure.service.user.UserService;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.user.UserRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserOrchestrator userOrchestrator;

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

    @GetMapping("/roles")
    public List<KeycloakRole> getAllRolesForUser() {
        return userService.getAllRoles();
    }

}
