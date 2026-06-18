package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.PeriodGradeService;
import com.rusobr.academic.web.controller.PeriodGradeController;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeTeacherResponse;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PeriodGradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PeriodGradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PeriodGradeService periodGradeService;

    private static final Long STUDENT_ID = 101L;
    private static final Long TEACHING_ASSIGNMENT_ID = 201L;
    private static final Long ACADEMIC_PERIOD_ID = 301L;
    private static final Long PERIOD_GRADE_ID = 401L;
    private static final String SCHOOL_YEAR = "2025/2026";
    private static final Integer VALUE = 5;

    private PeriodGradeStudentResponse buildStudentResponse() {
        return new PeriodGradeStudentResponse(PERIOD_GRADE_ID, VALUE, "Excellent work", "Mathematics", ACADEMIC_PERIOD_ID);
    }

    private PeriodGradeResponse buildPeriodGradeResponse() {
        return new PeriodGradeResponse(PERIOD_GRADE_ID, VALUE, "Excellent work", STUDENT_ID, ACADEMIC_PERIOD_ID);
    }

    private UserFeignResponse buildUserFeignResponse() {
        return new UserFeignResponse(STUDENT_ID, "Ivan", "Petrov", "ipetrov", "keycloak-1");
    }

    private PeriodGradeTeacherResponse buildTeacherResponse() {
        return new PeriodGradeTeacherResponse(
                buildUserFeignResponse(),
                List.of(buildPeriodGradeResponse()),
                4.75
        );
    }

    @Test
    @DisplayName("GET /period-grades/by-student — 200 and student period grades map")
    void getByStudentId_ShouldReturn200() throws Exception {
        when(periodGradeService.getByStudentId(STUDENT_ID, 1L))
                .thenReturn(Map.of("Mathematics", List.of(buildStudentResponse())));

        mockMvc.perform(get("/api/v1/period-grades/by-student")
                        .param("studentId", String.valueOf(STUDENT_ID))
                        .param("academicYearId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mathematics[0].id").value(PERIOD_GRADE_ID))
                .andExpect(jsonPath("$.Mathematics[0].subjectName").value("Mathematics"));
    }

    @Test
    @DisplayName("GET /period-grades/by-assignment — 200 and teacher period grades list")
    void getGradesByAssignment_ShouldReturn200() throws Exception {
        when(periodGradeService.getByAssignmentWithAverage(TEACHING_ASSIGNMENT_ID, ACADEMIC_PERIOD_ID, 1L))
                .thenReturn(List.of(buildTeacherResponse()));

        mockMvc.perform(get("/api/v1/period-grades/by-assignment")
                        .param("teachingAssignmentId", String.valueOf(TEACHING_ASSIGNMENT_ID))
                        .param("currentAcademicPeriodId", String.valueOf(ACADEMIC_PERIOD_ID))
                        .param("academicYearId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user.id").value(STUDENT_ID))
                .andExpect(jsonPath("$[0].periodGrades[0].id").value(PERIOD_GRADE_ID))
                .andExpect(jsonPath("$[0].currentAverage").value(4.75));
    }

    @Test
    @DisplayName("POST /period-grades — 201 and create period grade")
    void createPeriodGrade_ShouldReturn201() throws Exception {
        PeriodGradeRequest request = new PeriodGradeRequest(VALUE, "Excellent work", TEACHING_ASSIGNMENT_ID, STUDENT_ID, ACADEMIC_PERIOD_ID);
        when(periodGradeService.create(request)).thenReturn(buildPeriodGradeResponse());

        mockMvc.perform(post("/api/v1/period-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PERIOD_GRADE_ID))
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID));
    }

    @Test
    @DisplayName("DELETE /period-grades/{id} — 204 on delete")
    void deletePeriodGrade_ShouldReturn204() throws Exception {
        doNothing().when(periodGradeService).delete(PERIOD_GRADE_ID);

        mockMvc.perform(delete("/api/v1/period-grades/{id}", PERIOD_GRADE_ID))
                .andExpect(status().isNoContent());

        verify(periodGradeService).delete(PERIOD_GRADE_ID);
    }
}
