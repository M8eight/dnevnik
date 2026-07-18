package com.rusobr.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.user.application.service.student.StudentService;
import com.rusobr.user.web.controller.StudentController;
import com.rusobr.user.web.dto.feign.AcademicYearResponse;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.UserExceptionCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private StudentService studentService;

    private static final Long STUDENT_ID = 1L;
    private static final Long PARENT_ID = 2L;

    @BeforeEach
    void setUpSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("user_id", STUDENT_ID)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of())
        );
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AcademicYearResponse buildAcademicYearResponse() {
        return new AcademicYearResponse(
                2L,
                "2024-2025",
                "Учебный год 2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 5, 31),
                true
        );
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/students/{id}/details
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /students/{id}/details — 200 и тело ответа")
    void getDetailsById_ShouldReturn200() throws Exception {
        StudentDetails details = new StudentDetails("math");
        when(studentService.getDetailsById(STUDENT_ID)).thenReturn(details);

        mockMvc.perform(get("/api/v1/students/{id}/details", STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyProfile").value("math"));
    }

    @Test
    @DisplayName("GET /students/{id}/details — 404 если студент не найден")
    void getDetailsById_ShouldReturn404_WhenNotFound() throws Exception {
        when(studentService.getDetailsById(STUDENT_ID))
                .thenThrow(new NotFoundException("Student not found: " + STUDENT_ID, UserExceptionCode.STUDENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/students/{id}/details", STUDENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found: " + STUDENT_ID));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/students/batch
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /students/batch — 200 и список UserFeignResponse")
    void getBatch_ShouldReturn200() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        BatchUserResponse responses = new BatchUserResponse(List.of(
                new UserFeignResponse(1L, "Ivan", "Ivanov", "ivan", "kc-1"),
                new UserFeignResponse(2L, "Petr", "Petrov", "petr", "kc-2")
        ), List.of());

        when(studentService.getBatch(ids)).thenReturn(responses);

                mockMvc.perform(post("/api/v1/students/batch")
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
    @DisplayName("POST /students/batch — 200 пустой список если ids пустой")
    void getBatch_ShouldReturnEmptyList_WhenIdsEmpty() throws Exception {
        when(studentService.getBatch(List.of())).thenReturn(new BatchUserResponse(List.of(), List.of()));

                mockMvc.perform(post("/api/v1/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"found\":[],\"notFound\":[]}"));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/students/exclude-assigned
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /students/exclude-assigned — 200 и список студентов без назначенных")
    void getBatchWithExcludingIds_ShouldReturn200() throws Exception {
        Set<Long> excludeIds = Set.of(1L, 2L);
        List<UserFeignResponse> responses = List.of(
                new UserFeignResponse(3L, "Sidor", "Sidorov", "sidor", "kc-3")
        );
        when(studentService.getBatchWithExcludingIds(excludeIds)).thenReturn(responses);

        mockMvc.perform(post("/api/v1/students/exclude-assigned")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excludeIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].username").value("sidor"));
    }

    @Test
    @DisplayName("POST /students/exclude-assigned — 200 пустой Set возвращает всех студентов")
    void getBatchWithExcludingIds_ShouldReturnAll_WhenIdsEmpty() throws Exception {
        List<UserFeignResponse> responses = List.of(
                new UserFeignResponse(1L, "Ivan", "Ivanov", "ivan", "kc-1")
        );
        when(studentService.getBatchWithExcludingIds(Set.of())).thenReturn(responses);

        mockMvc.perform(post("/api/v1/students/exclude-assigned")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/students/{studentId}/assign/{teacherId}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /students/{studentId}/assign/{parentId} — 200 при успешной привязке")
    void assignToParent_ShouldReturn200() throws Exception {
        doNothing().when(studentService).assignToParent(STUDENT_ID, PARENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/assign/{parentId}", STUDENT_ID, PARENT_ID))
                .andExpect(status().isOk());

        verify(studentService).assignToParent(STUDENT_ID, PARENT_ID);
    }

    @Test
    @DisplayName("PATCH /students/{studentId}/assign/{parentId} — 404 если студент не найден")
    void assignToParent_ShouldReturn404_WhenStudentNotFound() throws Exception {
        doThrow(new NotFoundException("Student not found: " + STUDENT_ID, UserExceptionCode.STUDENT_NOT_FOUND))
                .when(studentService).assignToParent(STUDENT_ID, PARENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/assign/{parentId}", STUDENT_ID, PARENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found: " + STUDENT_ID));
    }

    @Test
    @DisplayName("PATCH /students/{studentId}/assign/{parentId} — 404 если родитель не найден")
    void assignToParent_ShouldReturn404_WhenParentNotFound() throws Exception {
        doThrow(new NotFoundException("Parent not found: " + PARENT_ID, UserExceptionCode.PARENT_NOT_FOUND))
                .when(studentService).assignToParent(STUDENT_ID, PARENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/assign/{parentId}", STUDENT_ID, PARENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parent not found: " + PARENT_ID));
    }

    @Test
    @DisplayName("PATCH /students/{studentId}/assign/{parentId} — 409 если студент уже привязан")
    void assignToParent_ShouldReturn409_WhenAlreadyAssigned() throws Exception {
        doThrow(new ConflictException("Student already has parent", UserExceptionCode.STUDENT_ALREADY_HAS_PARENT))
                .when(studentService).assignToParent(STUDENT_ID, PARENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/assign/{parentId}", STUDENT_ID, PARENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Student already has parent"));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/students/{studentId}/unassign
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /students/{studentId}/unassign — 200 при успешном отвязывании")
    void unassignFromParent_ShouldReturn200() throws Exception {
        doNothing().when(studentService).unassignFromParent(STUDENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/unassign", STUDENT_ID))
                .andExpect(status().isOk());

        verify(studentService).unassignFromParent(STUDENT_ID);
    }

    @Test
    @DisplayName("PATCH /students/{studentId}/unassign — 404 если студент не найден")
    void unassignFromParent_ShouldReturn404_WhenStudentNotFound() throws Exception {
        doThrow(new NotFoundException("Student not found: " + STUDENT_ID, UserExceptionCode.STUDENT_NOT_FOUND))
                .when(studentService).unassignFromParent(STUDENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/unassign", STUDENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found: " + STUDENT_ID));
    }

    @Test
    @DisplayName("PATCH /students/{studentId}/unassign — 409 если у студента нет родителя")
    void unassignFromParent_ShouldReturn409_WhenNoParent() throws Exception {
        doThrow(new ConflictException("Student has no parent", UserExceptionCode.STUDENT_HAS_NO_PARENT))
                .when(studentService).unassignFromParent(STUDENT_ID);

        mockMvc.perform(patch("/api/v1/students/{studentId}/unassign", STUDENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Student has no parent"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/students/{id}/with-class
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /students/with-class — 200 и тело ответа")
    void getWithClassById_ShouldReturn200() throws Exception {
        SchoolClassResponse schoolClass = new SchoolClassResponse(10L, "10А", buildAcademicYearResponse(), 5L);
        TeacherResponse teacher = new TeacherResponse(
                UserResponse.builder().id(5L).build(),
                new TeacherDetails("t@mail.com", "+7999")
        );
        StudentWithClassResponse response = new StudentWithClassResponse(
                STUDENT_ID, "Ivan", "Ivanov", "math", schoolClass, teacher
        );
        when(studentService.getWithClassById(STUDENT_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/students/with-class")
                        .with(jwt().jwt(builder -> builder.claim("user_id", STUDENT_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(STUDENT_ID))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.studyProfile").value("math"))
                .andExpect(jsonPath("$.schoolClass.id").value(10L))
                .andExpect(jsonPath("$.schoolClass.name").value("10А"))
                .andExpect(jsonPath("$.schoolClassTeacher.user.id").value(5L))
                .andExpect(jsonPath("$.schoolClassTeacher.details.email").value("t@mail.com"));
    }

    @Test
    @DisplayName("GET /students/with-class — 404 если студент не найден")
    void getWithClassById_ShouldReturn404_WhenNotFound() throws Exception {
        when(studentService.getWithClassById(STUDENT_ID))
                .thenThrow(new NotFoundException("Student not found", UserExceptionCode.STUDENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/students/with-class")
                        .with(jwt().jwt(builder -> builder.claim("user_id", STUDENT_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found"));
    }
}
