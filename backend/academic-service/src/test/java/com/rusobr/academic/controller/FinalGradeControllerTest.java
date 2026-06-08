package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.FinalGradeService;
import com.rusobr.academic.web.controller.FinalGradeController;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeTeacherResponse;
import com.rusobr.academic.web.exception.ConflictException;
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

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FinalGradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FinalGradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private FinalGradeService finalGradeService;

    private static final Long STUDENT_ID = 10L;
    private static final Long TEACHING_ASSIGNMENT_ID = 20L;
    private static final Long FINAL_GRADE_ID = 30L;
    private static final String SCHOOL_YEAR = "2024-2025";

    private FinalGradeRequest buildRequest() {
        return new FinalGradeRequest(
                STUDENT_ID,
                SCHOOL_YEAR,
                5,
                "Excellent work",
                TEACHING_ASSIGNMENT_ID
        );
    }

    private FinalGradeCreateResponse buildCreateResponse() {
        return new FinalGradeCreateResponse(
                FINAL_GRADE_ID,
                STUDENT_ID,
                SCHOOL_YEAR,
                5,
                "Excellent work"
        );
    }

    private FinalGradeResponse buildGradeResponse() {
        return new FinalGradeResponse(
                FINAL_GRADE_ID,
                STUDENT_ID,
                SCHOOL_YEAR,
                5,
                "Excellent work",
                "Mathematics"
        );
    }

    private FinalGradeTeacherResponse buildTeacherResponse() {
        UserFeignResponse user = new UserFeignResponse(
                100L,
                "Ivan",
                "Petrov",
                "ipetrov",
                "keycloak-123"
        );
        return new FinalGradeTeacherResponse(user, List.of(buildGradeResponse()));
    }

    @Test
    @DisplayName("GET /final-grades/by-student — 200 and map response")
    void getByStudentId_ShouldReturn200() throws Exception {
        when(finalGradeService.getByStudentId(STUDENT_ID, SCHOOL_YEAR))
                .thenReturn(Map.of("Mathematics", buildGradeResponse()));

        mockMvc.perform(get("/api/v1/final-grades/by-student")
                        .param("studentId", String.valueOf(STUDENT_ID))
                        .param("schoolYear", SCHOOL_YEAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mathematics.id").value(FINAL_GRADE_ID))
                .andExpect(jsonPath("$.Mathematics.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.Mathematics.schoolYear").value(SCHOOL_YEAR))
                .andExpect(jsonPath("$.Mathematics.value").value(5))
                .andExpect(jsonPath("$.Mathematics.description").value("Excellent work"));
    }

    @Test
    @DisplayName("GET /final-grades/by-assignment — 200 and list response")
    void getByAssignmentId_ShouldReturn200() throws Exception {
        when(finalGradeService.getByAssignmentId(TEACHING_ASSIGNMENT_ID, SCHOOL_YEAR))
                .thenReturn(List.of(buildTeacherResponse()));

        mockMvc.perform(get("/api/v1/final-grades/by-assignment")
                        .param("teachingAssignmentId", String.valueOf(TEACHING_ASSIGNMENT_ID))
                        .param("schoolYear", SCHOOL_YEAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user.id").value(100L))
                .andExpect(jsonPath("$[0].user.firstName").value("Ivan"))
                .andExpect(jsonPath("$[0].finalGrades[0].id").value(FINAL_GRADE_ID))
                .andExpect(jsonPath("$[0].finalGrades[0].subjectName").value("Mathematics"));
    }

    @Test
    @DisplayName("POST /final-grades — 200 and created grade")
    void create_ShouldReturn200() throws Exception {
        FinalGradeRequest request = buildRequest();
        when(finalGradeService.create(request)).thenReturn(buildCreateResponse());

        mockMvc.perform(post("/api/v1/final-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FINAL_GRADE_ID))
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.schoolYear").value(SCHOOL_YEAR))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.description").value("Excellent work"));
    }

    @Test
    @DisplayName("POST /final-grades — 409 if school year is invalid")
    void create_ShouldReturn409_WhenSchoolYearInvalid() throws Exception {
        FinalGradeRequest request = buildRequest();
        doThrow(new ConflictException("Academic period not found with school year " + SCHOOL_YEAR))
                .when(finalGradeService).create(request);

        mockMvc.perform(post("/api/v1/final-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Academic period not found with school year " + SCHOOL_YEAR));
    }

    @Test
    @DisplayName("DELETE /final-grades/{id} — 200 on success")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(finalGradeService).delete(FINAL_GRADE_ID);

        mockMvc.perform(delete("/api/v1/final-grades/{id}", FINAL_GRADE_ID))
                .andExpect(status().isOk());

        verify(finalGradeService).delete(FINAL_GRADE_ID);
    }

    @Test
    @DisplayName("DELETE /final-grades/{id} — 404 if not found")
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Final grade with id " + FINAL_GRADE_ID + " not found"))
                .when(finalGradeService).delete(FINAL_GRADE_ID);

        mockMvc.perform(delete("/api/v1/final-grades/{id}", FINAL_GRADE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Final grade with id " + FINAL_GRADE_ID + " not found"));
    }
}
