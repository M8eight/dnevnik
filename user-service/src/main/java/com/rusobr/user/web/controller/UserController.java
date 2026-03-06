package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.UserService;
import com.rusobr.user.web.dto.keycloack.KeycloackUserResponse;
import com.rusobr.user.web.dto.keycloack.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloack.KeycloackUserRequest;
import com.rusobr.user.web.dto.keycloack.role.KeycloackRoleDto;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/create")
    public KeycloackUserResponse createUser(@RequestBody KeycloackUserRequest keycloackUserRequest) {
        return userService.createUser(keycloackUserRequest);
    }

    @DeleteMapping("/delete")
    public void deleteUser(@RequestParam String keycloackId) {
        userService.deleteUser(keycloackId);
    }


    @GetMapping("/roles")
    public List<KeycloackRoleDto> getAllRolesForUser() {
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
