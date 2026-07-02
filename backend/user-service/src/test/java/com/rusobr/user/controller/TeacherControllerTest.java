package com.rusobr.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.application.service.teacher.TeacherService;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.controller.TeacherController;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TeacherController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private TeacherService teacherService;

    private static final Long TEACHER_ID = 1L;

    // ─────────────────────────────────────────────
    // GET /api/v1/teachers/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /teachers/{id} — 200 и тело ответа")
    void getWithUserById_ShouldReturn200() throws Exception {
        TeacherResponse response = new TeacherResponse(
                UserResponse.builder().id(TEACHER_ID).build(),
                new TeacherDetails("t@mail.com", "+7999")
        );
        when(teacherService.getWithUserById(TEACHER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/teachers/{id}", TEACHER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(TEACHER_ID))
                .andExpect(jsonPath("$.details.email").value("t@mail.com"))
                .andExpect(jsonPath("$.details.phoneNumber").value("+7999"));
    }

    @Test
    @DisplayName("GET /teachers/{id} — 404 если учитель не найден")
    void getWithUserById_ShouldReturn404_WhenNotFound() throws Exception {
        when(teacherService.getWithUserById(TEACHER_ID))
                .thenThrow(new NotFoundException("Teacher with id " + TEACHER_ID + " not found"));

        mockMvc.perform(get("/api/v1/teachers/{id}", TEACHER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher with id " + TEACHER_ID + " not found"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/teachers/{id}/details
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /teachers/{id}/details — 200 и тело ответа")
    void getDetailsById_ShouldReturn200() throws Exception {
        TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
        when(teacherService.getDetailsById(TEACHER_ID)).thenReturn(details);

        mockMvc.perform(get("/api/v1/teachers/{id}/details", TEACHER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("t@mail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+7999"));
    }

    @Test
    @DisplayName("GET /teachers/{id}/details — 404 если учитель не найден")
    void getDetailsById_ShouldReturn404_WhenNotFound() throws Exception {
        when(teacherService.getDetailsById(TEACHER_ID))
                .thenThrow(new NotFoundException("Teacher not found: " + TEACHER_ID));

        mockMvc.perform(get("/api/v1/teachers/{id}/details", TEACHER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher not found: " + TEACHER_ID));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/teachers/batch
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /teachers/batch — 200 и список UserFeignResponse")
    void findBatchTeachers_ShouldReturn200() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        BatchUserResponse responses = new BatchUserResponse(List.of(
                new UserFeignResponse(1L, "Ivan", "Ivanov", "ivan", "kc-1"),
                new UserFeignResponse(2L, "Petr", "Petrov", "petr", "kc-2")
        ), List.of());
        when(teacherService.getBatch(ids)).thenReturn(responses);

                mockMvc.perform(post("/api/v1/teachers/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found[0].id").value(1L))
                .andExpect(jsonPath("$.found[0].username").value("ivan"))
                .andExpect(jsonPath("$.found[1].id").value(2L))
                .andExpect(jsonPath("$.found[1].username").value("petr"))
                .andExpect(jsonPath("$.notFound").isArray());
    }

    @Test
    @DisplayName("POST /teachers/batch — 200 пустой список если ids пустой")
    void findBatchTeachers_ShouldReturnEmptyList_WhenIdsEmpty() throws Exception {
        when(teacherService.getBatch(List.of())).thenReturn(new BatchUserResponse(List.of(), List.of()));

                mockMvc.perform(post("/api/v1/teachers/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"found\":[],\"notFound\":[]}"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/teachers/{id}/simple
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /teachers/{id}/simple — 200 и тело ответа")
    void getTeacherSimpleById_ShouldReturn200() throws Exception {
        UserFeignResponse response = new UserFeignResponse(TEACHER_ID, "Ivan", "Ivanov", "ivan", "kc-1");
        when(teacherService.getSimpleById(TEACHER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/teachers/{id}/simple", TEACHER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEACHER_ID))
                .andExpect(jsonPath("$.username").value("ivan"))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"));
    }

    @Test
    @DisplayName("GET /teachers/{id}/simple — 404 если учитель не найден")
    void getTeacherSimpleById_ShouldReturn404_WhenNotFound() throws Exception {
        when(teacherService.getSimpleById(TEACHER_ID))
                .thenThrow(new NotFoundException("Teacher not found: " + TEACHER_ID));

        mockMvc.perform(get("/api/v1/teachers/{id}/simple", TEACHER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher not found: " + TEACHER_ID));
    }
}
