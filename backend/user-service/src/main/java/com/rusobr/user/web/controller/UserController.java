package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.user.UserOrchestrator;
import com.rusobr.user.infrastructure.service.user.UserService;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.student.StudentDetails;
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
    private final UserOrchestrator studentOrchestrator;

    @PostMapping("/students")
    public UserResponse createUser(@RequestBody UserRequest<StudentDetails> userRequest) {
        return studentOrchestrator.create(userRequest);
    }

    @GetMapping("/roles")
    public List<KeycloakRole> getAllRolesForUser() {
        return userService.getAllRoles();
    }

    @PostMapping("/roles")
    public void assignRoleToUser(@RequestBody AssignRoleToUserRequest assignRoleToUserRequest) {
        userService.assignRoleToUser(assignRoleToUserRequest);
    }

    @DeleteMapping("/roles")
    public void deleteRoleFromUser(@RequestBody AssignRoleToUserRequest assignRoleToUserRequest) {
        userService.deleteRoleFromUser(assignRoleToUserRequest);
    }

}
