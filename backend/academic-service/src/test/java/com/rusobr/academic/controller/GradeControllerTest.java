package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.GradeService;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.controller.GradeController;
import com.rusobr.academic.web.dto.grade.GradeResponse;

import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.ExceptionCode;
import com.rusobr.academic.web.exception.NotFoundException;
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

    private static final Long GRADE_ID = 1L;
    private static final Long STUDENT_ID = 10L;
    private static final Long PERIOD_ID = 5L;
    private static final Long LESSON_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);

    private GradeResponse buildGradeResponse() {
        return new GradeResponse(GRADE_ID, STUDENT_ID, 5, 2, GradeType.TEST);
    }

    private CreateGradeRequest buildCreateRequest() {
        return new CreateGradeRequest(STUDENT_ID, LESSON_ID, PERIOD_ID, 5, 2, GradeType.TEST);
    }

    private CreateGradeResponse buildCreateResponse() {
        return new CreateGradeResponse(GRADE_ID, STUDENT_ID, new LessonInstanceDto(LESSON_ID, DATE), 5, 2, GradeType.TEST);
    }

    @Test
    @DisplayName("GET /grades/{id} — 200 and grade details")
    void getById_ShouldReturn200() throws Exception {
        when(gradeService.getById(GRADE_ID)).thenReturn(buildGradeResponse());

        mockMvc.perform(get("/api/v1/grades/{id}", GRADE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeId").value(GRADE_ID))
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.weight").value(2))
                .andExpect(jsonPath("$.gradeType").value("TEST"));
    }

    @Test
    @DisplayName("GET /grades/{id} — 404 when grade not found")
    void getById_ShouldReturn404_WhenNotFound() throws Exception {
        when(gradeService.getById(GRADE_ID)).thenThrow(new NotFoundException("Grade with id " + GRADE_ID + " not found", ExceptionCode.GRADE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/grades/{id}", GRADE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Grade with id " + GRADE_ID + " not found"));
    }

    @Test
    @DisplayName("POST /grades — 200 and created grade")
    void create_ShouldReturn200() throws Exception {
        CreateGradeRequest request = buildCreateRequest();
        when(gradeService.create(request)).thenReturn(buildCreateResponse());

        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeId").value(GRADE_ID))
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.lessonInstance.id").value(LESSON_ID))
                .andExpect(jsonPath("$.lessonInstance.lessonDate").value(DATE.toString()))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.weight").value(2))
                .andExpect(jsonPath("$.gradeType").value("TEST"));
    }

    @Test
    @DisplayName("POST /grades — 409 when academic period is closed")
    void create_ShouldReturn409_WhenPeriodClosed() throws Exception {
        CreateGradeRequest request = buildCreateRequest();
        doThrow(new ConflictException("Academic period is already closed", ExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT))
                .when(gradeService).create(request);

        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Academic period is already closed"));
    }

    @Test
    @DisplayName("DELETE /grades/{id} — 200 on success")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(gradeService).delete(GRADE_ID);

        mockMvc.perform(delete("/api/v1/grades/{id}", GRADE_ID))
                .andExpect(status().isOk());

        verify(gradeService).delete(GRADE_ID);
    }

    @Test
    @DisplayName("DELETE /grades/{id} — 404 when grade not found")
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Grade with id " + GRADE_ID + " not found", ExceptionCode.GRADE_NOT_FOUND))
                .when(gradeService).delete(GRADE_ID);

        mockMvc.perform(delete("/api/v1/grades/{id}", GRADE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Grade with id " + GRADE_ID + " not found"));
    }
}
