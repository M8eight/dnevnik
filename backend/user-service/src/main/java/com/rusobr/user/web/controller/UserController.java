package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.UserService;
import com.rusobr.user.web.dto.keycloak.CreateUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findUserDbById(id);
    }

    @GetMapping("")
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @PostMapping("/batch")
    public List<UserResponse> getBatchUsers(@RequestBody List<Long> ids) {
        return userService.findBatchUsers(ids);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @DeleteMapping("/delete")
    public void deleteUser(@RequestParam String keycloakId) {
        userService.deleteUser(keycloakId);
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
