package com.rusobr.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.infrastructure.enums.UserRoles;
import com.rusobr.user.infrastructure.exception.KeycloackUserAlreadyExist;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.service.UserService;
import com.rusobr.user.web.controller.UserController;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.CreateUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserResponse;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUser() throws Exception {
        UserResponse res = new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L);
        when(userService.findUserDbById(1L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.keycloakId").value("abc-123"));
    }

    @Test
    void shouldUserNotFound() throws Exception {
        when(userService.findUserDbById(1L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldGetBatchUsers() throws Exception {
        List<UserResponse> users = List.of(new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L));
        List<Long> ids = List.of(1L);
        when(userService.findBatchUsers(any())).thenReturn(users);
        mockMvc.perform(post("/api/v1/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createUserShouldReturnUser() throws Exception {
        CreateUserRequest req =
                new CreateUserRequest("AlexK", "123456", "Алексей", "Кочетыгов");
        CreateUserResponse res =
                new CreateUserResponse(1L, "Алексей", "Кочетыгов", "AlexK", "abc-123");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.keycloakId").value("abc-123"));
    }

    @Test
    void createUser_shouldReturnConflict_whenExists() throws Exception {
        CreateUserRequest req =
                new CreateUserRequest("AlexK", "123456", "Алексей", "Кочетыгов");

        when(userService.createUser(any(CreateUserRequest.class))).thenThrow(new KeycloackUserAlreadyExist("Keycloack User already exists"));

        mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Keycloack User already exists"));
    }

    @Test
    @DisplayName("Должен вернуть ok после удаления пользователя")
    void deleteUser_shouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser("abc-123");

        mockMvc.perform(delete("/api/v1/users/delete")
                .param("keycloakId", "abc-123")
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получить всех пользователей")
    void getAllUsers_shouldReturnsUsers() throws Exception {
        UserResponse user = new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L);
        List<UserResponse> users = List.of(user);
        Page<UserResponse> page =
                new PageImpl<>(List.of(user), PageRequest.of(0, 10), users.size());

        when(userService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Получить все роли")
    void getAllRoles_shouldReturnRoles() throws Exception {
        when(userService.getAllRoles()).thenReturn(List.of(new KeycloakRole("role-123", "ADMIN")));

        mockMvc.perform(get("/api/v1/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("role-123"));
    }

    @Test
    @DisplayName("Присвоить роль пользователю")
    void assignRoleToUser_shouldAssignRoleToUser() throws Exception {
        AssignRoleToUserRequest dto = new AssignRoleToUserRequest("user-123", UserRoles.ADMIN, "role-123");

        doNothing().when(userService).assignRoleToUser(dto);

        userService.assignRoleToUser(dto);

        mockMvc.perform(post("/api/v1/users/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk());
    }


    @Test
    @DisplayName("Присвоить роль пользователю, пользователь не найден")
    void assignRoleToUser_shouldReturnConflictUserNotFound() throws Exception {
        AssignRoleToUserRequest dto =
                new AssignRoleToUserRequest("user-123", UserRoles.ADMIN, "role-123");

        doThrow(new NotFoundException("User not found")).when(userService).assignRoleToUser(dto);


        mockMvc.perform(post("/api/v1/users/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Отвязать роль от пользователя")
    void deleteRoleFromUser_shouldDeleteRoleFromUsers() throws Exception {
        AssignRoleToUserRequest dto =
                new AssignRoleToUserRequest("user-123", UserRoles.ADMIN, "role-123");

        doNothing().when(userService).deleteRoleFromUser(dto);

        userService.deleteRoleFromUser(dto);

        mockMvc.perform(delete("/api/v1/users/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Отвязать роль от пользователя, пользователь не найден")
    void deleteRoleFromUser_shouldReturnConflictUserNotFound() throws Exception {
        AssignRoleToUserRequest dto =
                new AssignRoleToUserRequest("user-123", UserRoles.ADMIN, "role-123");

        doThrow(new NotFoundException("User not found")).when(userService).deleteRoleFromUser(dto);


        mockMvc.perform(delete("/api/v1/users/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isNotFound());
    }


}
