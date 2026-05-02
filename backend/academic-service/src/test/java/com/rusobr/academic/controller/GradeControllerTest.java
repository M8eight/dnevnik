package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.controller.GradeController;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private GradeService gradeService;

    // ─────────────────────────────────────────────
    // GET /api/v1/grades/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /grades/{id} — 200 и тело ответа")
    void getGradeById_ShouldReturn200() throws Exception {
        GradeResponse dto = new GradeResponse(1L, 10L, 5, GradeType.TEST);
        when(gradeService.getGradeById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/grades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeId").value(1))
                .andExpect(jsonPath("$.studentId").value(10))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.gradeType").value("TEST"));
    }

    @Test
    @DisplayName("GET /grades/{id} — 404 если оценка не найдена")
    void getGradeById_ShouldReturn404_WhenNotFound() throws Exception {
        when(gradeService.getGradeById(99L))
                .thenThrow(new NotFoundException("Grade not found gradeId: 99"));

        mockMvc.perform(get("/api/v1/grades/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Grade not found gradeId: 99"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/grades/avg/by-student/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /grades/avg/by-student/{id} — 200 и средняя оценка")
    void getAverageGrade_ShouldReturn200() throws Exception {
        when(gradeService.getAverageGrade(1L, 2L)).thenReturn(4.5);

        mockMvc.perform(get("/api/v1/grades/avg/by-student/1")
                        .param("academicPeriodId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }

    @Test
    @DisplayName("GET /grades/avg/by-student/{id} — 404 если период не найден")
    void getAverageGrade_ShouldReturn404_WhenPeriodNotFound() throws Exception {
        when(gradeService.getAverageGrade(1L, 99L))
                .thenThrow(new NotFoundException("Academic period not found academicPeriodId: 99"));

        mockMvc.perform(get("/api/v1/grades/avg/by-student/1")
                        .param("academicPeriodId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Academic period not found academicPeriodId: 99"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/grades/by-date
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /grades/by-date — 200 и список оценок")
    void findAllGradesByDate_ShouldReturn200() throws Exception {
        List<GradeWithSubjectNameResponse> grades = List.of(
                new GradeWithSubjectNameResponse(1L, 5, GradeType.TEST, "Математика")
        );
        when(gradeService.findAllGradesByDate(1L, LocalDate.of(2025, 9, 1))).thenReturn(grades);

        mockMvc.perform(get("/api/v1/grades/by-date")
                        .param("studentId", "1")
                        .param("date", "2025-09-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].value").value(5))
                .andExpect(jsonPath("$[0].gradeType").value("TEST"))
                .andExpect(jsonPath("$[0].subjectName").value("Математика"));
    }

    @Test
    @DisplayName("GET /grades/by-date — 200 пустой список если оценок нет")
    void findAllGradesByDate_ShouldReturnEmptyList_WhenNoGrades() throws Exception {
        when(gradeService.findAllGradesByDate(1L, LocalDate.of(2025, 9, 1))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/grades/by-date")
                        .param("studentId", "1")
                        .param("date", "2025-09-01"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/grades
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /grades — 200 и тело ответа при успешном создании")
    void createGrade_ShouldReturn200() throws Exception {
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, 2L, 5, 1, GradeType.TEST);
        LessonInstanceDto lessonInstanceDto = new LessonInstanceDto(10L, LocalDate.of(2025, 9, 1));
        CreateGradeResponse response = new CreateGradeResponse(100L, 1L, lessonInstanceDto, 5, 1, GradeType.TEST);

        when(gradeService.createGrade(any(CreateGradeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeId").value(100))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.weight").value(1))
                .andExpect(jsonPath("$.gradeType").value("TEST"))
                .andExpect(jsonPath("$.lessonInstance.id").value(10))
                .andExpect(jsonPath("$.lessonInstance.lessonDate").value("2025-09-01"));
    }

    @Test
    @DisplayName("POST /grades — 400 если тело запроса не валидно")
    void createGrade_ShouldReturn400_WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    @DisplayName("POST /grades — 404 если LessonInstance не найден")
    void createGrade_ShouldReturn404_WhenLessonInstanceNotFound() throws Exception {
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, 2L, 5, 1, GradeType.TEST);

        when(gradeService.createGrade(any())).thenThrow(new NotFoundException("Lesson instance not found"));

        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Lesson instance not found"));
    }

    @Test
    @DisplayName("POST /grades — 404 если период не найден")
    void createGrade_ShouldReturn404_WhenPeriodNotFound() throws Exception {
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, 2L, 5, 1, GradeType.TEST);

        when(gradeService.createGrade(any())).thenThrow(new NotFoundException("Academic period not found"));

        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Academic period not found"));
    }

    @Test
    @DisplayName("POST /grades — 409 если период закрыт")
    void createGrade_ShouldReturn409_WhenPeriodIsClosed() throws Exception {
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, 2L, 5, 1, GradeType.TEST);

        when(gradeService.createGrade(any())).thenThrow(new ConflictException("Academic period is already closed"));

        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Academic period is already closed"));
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/grades/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /grades/{id} — 200 при успешном удалении")
    void deleteGrade_ShouldReturn200() throws Exception {
        doNothing().when(gradeService).deleteGrade(1L);

        mockMvc.perform(delete("/api/v1/grades/1"))
                .andExpect(status().isOk());

        verify(gradeService).deleteGrade(1L);
    }

    @Test
    @DisplayName("DELETE /grades/{id} — 404 если оценка не найдена")
    void deleteGrade_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Grade not found gradeId: 99"))
                .when(gradeService).deleteGrade(99L);

        mockMvc.perform(delete("/api/v1/grades/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Grade not found gradeId: 99"));
    }

    @Test
    @DisplayName("DELETE /grades/{id} — 409 если период закрыт")
    void deleteGrade_ShouldReturn409_WhenPeriodIsClosed() throws Exception {
        doThrow(new ConflictException("Academic period is closed"))
                .when(gradeService).deleteGrade(1L);

        mockMvc.perform(delete("/api/v1/grades/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Academic period is closed"));
    }
}