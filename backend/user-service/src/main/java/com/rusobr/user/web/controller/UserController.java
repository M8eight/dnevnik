package com.rusobr.user.web.controller;

import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.application.service.user.UserOrchestrator;
import com.rusobr.user.application.service.user.UserService;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.update.UserUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserOrchestrator userOrchestrator;

    @GetMapping
    public Page<UserResponse> getUsers(Pageable pageable,
                                       @RequestParam(required = false) UserRole role,
                                       @RequestParam(required = false) String search) {
        return userService.getAllByFilter(pageable, role, search);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public void createStudent(@RequestBody @Valid UserCreateRequest<StudentDetails> userRequest) {
        userOrchestrator.create(userRequest);
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTeacher(@RequestBody @Valid UserCreateRequest<TeacherDetails> userRequest) {
        userOrchestrator.create(userRequest);
    }

    @PostMapping("/parents")
    @ResponseStatus(HttpStatus.CREATED)
    public void createParent(@RequestBody @Valid UserCreateRequest<ParentDetails> userRequest) {
        userOrchestrator.create(userRequest);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return userOrchestrator.update(id, userUpdateRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NotNull Long id) {
        userService.deleteUserCascade(id);
    }

}
