package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.controller.JournalController;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.DatesGradesDto;
import com.rusobr.academic.web.dto.lessonInstance.GradeLessonDto;
import com.rusobr.academic.web.dto.lessonInstance.GradesLessonsResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JournalController.class)
@AutoConfigureMockMvc(addFilters = false)
public class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JournalService lessonInstanceService;

    private static final Long STUDENT_ID = 42L;
    private static final Long PERIOD_ID = 5L;
    private static final Long ASSIGNMENT_ID = 7L;
    private static final Long LESSON_INSTANCE_ID = 100L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);

    @BeforeEach
    void setUpJwt() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("user_id", STUDENT_ID)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private AcademicYearResponse buildAcademicYearResponse() {
        return new AcademicYearResponse(
                2L,                                // id учебного года
                "2024-2025",                       // name
                "Учебный год 2024-2025",           // description (может быть null)
                LocalDate.of(2024, 9, 1),          // startDate
                LocalDate.of(2025, 5, 31),         // endDate
                true                               // isActive
        );
    }

    private AcademicPeriodResponse buildAcademicPeriodResponse() {
        return new AcademicPeriodResponse(PERIOD_ID, "Q1", buildAcademicYearResponse(), false, START_DATE, START_DATE.plusMonths(3));
    }

    private GradesLessonsResponse buildGradesLessonsResponse() {
        GradeLessonDto gradeLesson = new GradeLessonDto(1L, 5, 1, GradeType.CONTROL, START_DATE);
        DatesGradesDto datesGrades = new DatesGradesDto("Mathematics", List.of(gradeLesson));
        return new GradesLessonsResponse(buildAcademicPeriodResponse(), List.of(START_DATE), List.of(datesGrades));
    }

    private TeacherJournalResponse buildTeacherJournalResponse() {
        UserFeignResponse student = new UserFeignResponse(STUDENT_ID, "Ivan", "Ivanov", "ivan", "keycloak-1");
        LessonInstanceDto lessonInstance = new LessonInstanceDto(LESSON_INSTANCE_ID, START_DATE);
        StudentJournalDto studentJournal = new StudentJournalDto(
                STUDENT_ID,
                List.of(new StudentJournalDto.GradeLessonTeacherDto(1L, 5, 1, GradeType.CONTROL, LESSON_INSTANCE_ID)),
                5.0,
                List.of()
        );
        return new TeacherJournalResponse(buildAcademicPeriodResponse(), new BatchUserResponse(List.of(student), List.of()), List.of(lessonInstance), List.of(studentJournal));
    }

    @Test
    @DisplayName("GET /grades/by-student — 200 and grades lessons response")
    void getGradesByStudentId_ShouldReturn200() throws Exception {
        GradesLessonsResponse response = buildGradesLessonsResponse();
        when(lessonInstanceService.getGradesLessonsByStudentId(STUDENT_ID, PERIOD_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/grades/by-student")
                        .param("academicPeriodId", String.valueOf(PERIOD_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicPeriod.id").value(PERIOD_ID))
                .andExpect(jsonPath("$.dates[0]").value(START_DATE.toString()))
                .andExpect(jsonPath("$.subjects[0].subject").value("Mathematics"))
                .andExpect(jsonPath("$.subjects[0].grades[0].gradeId").value(1))
                .andExpect(jsonPath("$.subjects[0].grades[0].gradeType").value("CONTROL"));
    }

    @Test
    @DisplayName("GET /grades/by-student — 404 when period not found")
    void getGradesByStudentId_ShouldReturn404_WhenPeriodNotFound() throws Exception {
        when(lessonInstanceService.getGradesLessonsByStudentId(STUDENT_ID, PERIOD_ID))
                .thenThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found", AcademicExceptionCode.ACADEMIC_PERIOD_NOT_FOUND));

        mockMvc.perform(get("/api/v1/grades/by-student")
                        .param("academicPeriodId", String.valueOf(PERIOD_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Academic period with id " + PERIOD_ID + " not found"));
    }

    @Test
    @DisplayName("GET /journal/by-assignment — 200 and teacher journal response")
    void getByAssignment_ShouldReturn200() throws Exception {
        when(lessonInstanceService.getJournalByAssignment(ASSIGNMENT_ID, PERIOD_ID)).thenReturn(buildTeacherJournalResponse());

        mockMvc.perform(get("/api/v1/journal/by-assignment")
                        .param("teachingAssignmentId", String.valueOf(ASSIGNMENT_ID))
                        .param("academicPeriodId", String.valueOf(PERIOD_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicPeriod.id").value(PERIOD_ID))
                .andExpect(jsonPath("$.students.found[0].id").value(STUDENT_ID))
                .andExpect(jsonPath("$.lessonInstances[0].id").value(LESSON_INSTANCE_ID));
    }

    @Test
    @DisplayName("GET /lesson-instances/by-assignment — 200 and lesson instances list")
    void getInstanceByAssignment_ShouldReturn200() throws Exception {
        List<LessonInstanceDto> instances = List.of(new LessonInstanceDto(LESSON_INSTANCE_ID, START_DATE));
        when(lessonInstanceService.getInstancesByAssignment(ASSIGNMENT_ID, PERIOD_ID)).thenReturn(instances);

        mockMvc.perform(get("/api/v1/lesson-instances/by-assignment")
                        .param("teachingAssignmentId", String.valueOf(ASSIGNMENT_ID))
                        .param("academicPeriodId", String.valueOf(PERIOD_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(LESSON_INSTANCE_ID))
                .andExpect(jsonPath("$[0].lessonDate").value(START_DATE.toString()));
    }
}
