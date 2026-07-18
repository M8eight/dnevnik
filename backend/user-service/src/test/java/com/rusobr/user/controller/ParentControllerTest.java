package com.rusobr.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.user.application.service.parent.ParentService;
import com.rusobr.user.web.controller.ParentController;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.UserExceptionCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ParentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ParentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private ParentService parentService;

    private static final Long PARENT_ID = 1L;

    // ─────────────────────────────────────────────
    // GET /api/v1/parents/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /parents/{id} — 200 и тело ответа")
    void getWithUserById_ShouldReturn200() throws Exception {
        UserResponse parentUser = UserResponse.builder()
                .id(PARENT_ID)
                .firstName("Ivan")
                .lastName("Ivanov")
                .username("ivan")
                .build();
        UserResponse child = UserResponse.builder()
                .id(2L)
                .firstName("Petr")
                .lastName("Ivanov")
                .username("petr")
                .build();
        ParentResponse response = new ParentResponse(parentUser, Set.of(child));
        when(parentService.getWithUserById(PARENT_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/parents/{id}", PARENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(PARENT_ID))
                .andExpect(jsonPath("$.user.firstName").value("Ivan"))
                .andExpect(jsonPath("$.user.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.user.username").value("ivan"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children[0].id").value(2L));
    }

    @Test
    @DisplayName("GET /parents/{id} — 200 с пустым списком детей")
    void getWithUserById_ShouldReturn200_WhenNoChildren() throws Exception {
        UserResponse parentUser = UserResponse.builder().id(PARENT_ID).build();
        ParentResponse response = new ParentResponse(parentUser, Set.of());
        when(parentService.getWithUserById(PARENT_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/parents/{id}", PARENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(PARENT_ID))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children").isEmpty());
    }

    @Test
    @DisplayName("GET /parents/{id} — 404 если родитель не найден")
    void getWithUserById_ShouldReturn404_WhenNotFound() throws Exception {
        when(parentService.getWithUserById(PARENT_ID))
                .thenThrow(new NotFoundException("Parent not found: " + PARENT_ID, UserExceptionCode.PARENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/parents/{id}", PARENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parent not found: " + PARENT_ID));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/parents/{id}/details
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /parents/{id}/details — 200 и тело ответа")
    void getDetailsById_ShouldReturn200() throws Exception {
        ParentDetails details = new ParentDetails();
        when(parentService.getDetailsById(PARENT_ID)).thenReturn(details);

        mockMvc.perform(get("/api/v1/parents/{id}/details", PARENT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /parents/{id}/details — 404 если родитель не найден")
    void getDetailsById_ShouldReturn404_WhenNotFound() throws Exception {
        when(parentService.getDetailsById(PARENT_ID))
                .thenThrow(new NotFoundException("Parent not found: " + PARENT_ID, UserExceptionCode.PARENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/parents/{id}/details", PARENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parent not found: " + PARENT_ID));
    }
}