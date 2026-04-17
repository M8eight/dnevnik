package com.rusobr.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.infrastructure.enums.UserRoles;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.service.UserService;
import com.rusobr.user.web.controller.UserController;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.CreateUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private UserService userService;

    // ─────────────────────────────────────────────
    // GET /api/v1/users/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /{id} — 200 OK")
    void getUser_ShouldReturnUser() throws Exception {
        UserResponse response = new UserResponse("Ivan", "Ivanov", "kc-123", 1L);
        when(userService.findUserDbById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.keycloakId").value("kc-123"));
    }

    @Test
    @DisplayName("GET /{id} — 404 Not Found")
    void getUser_ShouldReturn404() throws Exception {
        when(userService.findUserDbById(99L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/users (Pagination)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET / — 200 OK с пагинацией")
    void getUsers_ShouldReturnPage() throws Exception {
        UserResponse user = new UserResponse("Ivan", "Ivanov", "kc-123", 1L);
        PageImpl<UserResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Ivan"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/users/create
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /create — 201 Created")
    void createUser_ShouldReturn201() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("vanya")
                .firstName("Ivan")
                .lastName("Ivanov")
                .password("secret")
                .build();

        CreateUserResponse response = CreateUserResponse.builder()
                .id(1L)
                .keycloakId("kc-uuid")
                .username("vanya")
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keycloakId").value("kc-uuid"))
                .andExpect(jsonPath("$.id").value(1));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/users/batch
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /batch — 200 OK со списком ID")
    void getBatchUsers_ShouldReturnList() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        List<UserResponse> responses = List.of(
                new UserResponse("Ivan", "Ivanov", "kc-1", 1L),
                new UserResponse("Petr", "Petrov", "kc-2", 2L)
        );

        when(userService.findBatchUsers(ids)).thenReturn(responses);

        mockMvc.perform(post("/api/v1/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].lastName").value("Petrov"));
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/users/delete
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /delete — 200 OK")
    void deleteUser_ShouldReturn200() throws Exception {
        String kcId = "some-uuid";
        doNothing().when(userService).deleteUser(kcId);

        mockMvc.perform(delete("/api/v1/users/delete")
                        .param("keycloakId", kcId))
                .andExpect(status().isOk());

        verify(userService).deleteUser(kcId);
    }

    // ─────────────────────────────────────────────
    // Roles Operations
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /roles — 200 OK")
    void getAllRoles_ShouldReturnList() throws Exception {
        List<KeycloakRole> roles = List.of(new KeycloakRole("1", "ADMIN"));
        when(userService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(get("/api/v1/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /roles — 200 OK при назначении роли")
    void assignRole_ShouldReturn200() throws Exception {
        AssignRoleToUserRequest request = new AssignRoleToUserRequest("kc-id", UserRoles.TEACHER, "role-id");

        mockMvc.perform(post("/api/v1/users/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).assignRoleToUser(any());
    }

    @Test
    @DisplayName("DELETE /roles — 200 OK при удалении роли")
    void deleteRole_ShouldReturn200() throws Exception {
        AssignRoleToUserRequest request = new AssignRoleToUserRequest("kc-id", UserRoles.STUDENT, "role-id");

        mockMvc.perform(delete("/api/v1/users/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).deleteRoleFromUser(any());
    }
}