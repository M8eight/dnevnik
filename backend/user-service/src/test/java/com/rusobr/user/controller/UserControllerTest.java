package com.rusobr.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.application.service.user.UserOrchestrator;
import com.rusobr.user.application.service.user.UserService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.web.controller.UserController;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import com.rusobr.user.web.dto.user.update.UserUpdateRequest;
import com.rusobr.user.web.exception.ConflictException;
import com.rusobr.user.web.exception.ExceptionCode;
import com.rusobr.user.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserOrchestrator userOrchestrator;

    private static final Long USER_ID = 1L;

    private UserDataDto validUserDataDto() {
        return UserDataDto.builder()
                .username("ivan123")
                .password("secret123")
                .firstName("Ivan")
                .lastName("Ivanov")
                .build();
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/users
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /users — 200 и страница пользователей")
    void getUsers_ShouldReturn200() throws Exception {
        UserResponse userResponse = UserResponse.builder().id(USER_ID).build();
        when(userService.getAllByFilter(any(), isNull(), isNull()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(userResponse)));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(USER_ID));
    }

    @Test
    @DisplayName("GET /users — 200 с фильтрами role и search")
    void getUsers_WithFilters_ShouldReturn200() throws Exception {
        when(userService.getAllByFilter(any(), eq(UserRole.STUDENT), eq("Ivan")))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/users")
                        .param("role", "STUDENT")
                        .param("search", "Ivan"))
                .andExpect(status().isOk());

        verify(userService).getAllByFilter(any(), eq(UserRole.STUDENT), eq("Ivan"));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/users/students
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /users/students — 200 и тело ответа при успешном создании")
    void createStudent_ShouldReturn200() throws Exception {
        UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(
                validUserDataDto(), UserRole.STUDENT, new StudentDetails("math")
        );
        doNothing().when(userOrchestrator).create(any());

        mockMvc.perform(post("/api/v1/users/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /users/students — 400 если username null")
    void createStudent_ShouldReturn400_WhenUsernameNull() throws Exception {
        UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(
                UserDataDto.builder().password("secret123").firstName("Ivan").lastName("Ivanov").build(),
                UserRole.STUDENT, new StudentDetails("math")
        );

        mockMvc.perform(post("/api/v1/users/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userOrchestrator);
    }

    @Test
    @DisplayName("POST /users/students — 409 если создание не удалось")
    void createStudent_ShouldReturn409_WhenConflict() throws Exception {
        UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(
                validUserDataDto(), UserRole.STUDENT, new StudentDetails("math")
        );
        doThrow(new ConflictException("Could not create user", ExceptionCode.USER_CREATE_CONFLICT)).when(userOrchestrator).create(any());

        mockMvc.perform(post("/api/v1/users/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Could not create user"));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/users/teachers
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /users/teachers — 200 и тело ответа при успешном создании")
    void createTeacher_ShouldReturn200() throws Exception {
        UserCreateRequest<TeacherDetails> request = new UserCreateRequest<>(
                validUserDataDto(), UserRole.TEACHER, new TeacherDetails("t@mail.com", "+7999")
        );
        doNothing().when(userOrchestrator).create(any());

        mockMvc.perform(post("/api/v1/users/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /users/teachers — 400 если role null")
    void createTeacher_ShouldReturn400_WhenRoleNull() throws Exception {
        UserCreateRequest<TeacherDetails> request = new UserCreateRequest<>(
                validUserDataDto(), null, new TeacherDetails("t@mail.com", "+7999")
        );

        mockMvc.perform(post("/api/v1/users/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userOrchestrator);
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/users/parents
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /users/parents — 200 и тело ответа при успешном создании")
    void createParent_ShouldReturn200() throws Exception {
        UserCreateRequest<ParentDetails> request = new UserCreateRequest<>(
                validUserDataDto(), UserRole.PARENT, new ParentDetails()
        );
        doNothing().when(userOrchestrator).create(any());

        mockMvc.perform(post("/api/v1/users/parents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /users/parents — 409 если создание не удалось")
    void createParent_ShouldReturn409_WhenConflict() throws Exception {
        UserCreateRequest<ParentDetails> request = new UserCreateRequest<>(
                validUserDataDto(), UserRole.PARENT, new ParentDetails()
        );
        doThrow(new ConflictException("Could not create user", ExceptionCode.USER_CREATE_CONFLICT)).when(userOrchestrator).create(any());

        mockMvc.perform(post("/api/v1/users/parents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Could not create user"));
    }

    // ─────────────────────────────────────────────
    // PUT /api/v1/users/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PUT /users/{id} — 200 и тело ответа при успешном обновлении")
    void update_ShouldReturn200() throws Exception {
        UserUpdateData updateData = UserUpdateData.builder()
                .username("new_ivan").firstName("New").lastName("Ivanov").build();
        UserUpdateRequest request = new UserUpdateRequest(
                USER_ID, updateData, null,
                Set.of(UserRole.STUDENT),
                Map.of(UserRole.STUDENT, new StudentDetails("math"))
        );
        UserResponse expected = UserResponse.builder().id(USER_ID).build();
        when(userOrchestrator.update(eq(USER_ID), any())).thenReturn(expected);

        mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID));
    }

    @Test
    @DisplayName("PUT /users/{id} — 409 если username уже занят")
    void update_ShouldReturn409_WhenUsernameTaken() throws Exception {
        UserUpdateData updateData = UserUpdateData.builder().username("taken").build();
        UserUpdateRequest request = new UserUpdateRequest(
                USER_ID, updateData, null,
                Set.of(UserRole.STUDENT),
                Map.of(UserRole.STUDENT, new StudentDetails("math"))
        );
        when(userOrchestrator.update(eq(USER_ID), any()))
                .thenThrow(new ConflictException("Username already exists", ExceptionCode.USERNAME_ALREADY_EXISTS));

        mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("PUT /users/{id} — 404 если пользователь не найден")
    void update_ShouldReturn404_WhenUserNotFound() throws Exception {
        UserUpdateData updateData = UserUpdateData.builder().build();
        UserUpdateRequest request = new UserUpdateRequest(
                USER_ID, updateData, null,
                Set.of(UserRole.STUDENT),
                Map.of(UserRole.STUDENT, new StudentDetails("math"))
        );
        when(userOrchestrator.update(eq(USER_ID), any()))
                .thenThrow(new NotFoundException("User not found with id: " + USER_ID, ExceptionCode.USER_NOT_FOUND));

        mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: " + USER_ID));
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/users/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /users/{id} — 200 при успешном удалении")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(userService).deleteUserCascade(USER_ID);

        mockMvc.perform(delete("/api/v1/users/{id}", USER_ID))
                .andExpect(status().isOk());

        verify(userService).deleteUserCascade(USER_ID);
    }

    @Test
    @DisplayName("DELETE /users/{id} — 404 если пользователь не найден")
    void delete_ShouldReturn404_WhenUserNotFound() throws Exception {
        doThrow(new NotFoundException("User not found with id: " + USER_ID, ExceptionCode.USER_NOT_FOUND))
                .when(userService).deleteUserCascade(USER_ID);

        mockMvc.perform(delete("/api/v1/users/{id}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: " + USER_ID));
    }
}