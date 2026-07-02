package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.ClassStudentService;
import com.rusobr.academic.application.service.SchoolClassService;
import com.rusobr.academic.web.controller.SchoolClassController;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.TeacherDetails;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassUpdateRequest;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SchoolClassController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SchoolClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private SchoolClassService schoolClassService;

    @MockitoBean
    private ClassStudentService classStudentService;

    private static final Long SCHOOL_CLASS_ID = 11L;
    private static final Long STUDENT_ID = 21L;
    private static final Long TEACHER_ID = 31L;
    private static final String NAME = "10A";
    private static final String YEAR = "2025/2026";

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

    private SchoolClassResponse buildSchoolClassResponse() {
        return new SchoolClassResponse(SCHOOL_CLASS_ID, NAME, buildAcademicYearResponse(), TEACHER_ID);
    }

    private UserFeignResponse buildStudentResponse() {
        return new UserFeignResponse(STUDENT_ID, "Ivan", "Petrov", "ipetrov", "keycloak-1");
    }

    private TeacherResponse buildTeacherResponse() {
        return new TeacherResponse(
                new UserFeignResponse(TEACHER_ID, "Anna", "Ivanova", "aivanova", "keycloak-2"),
                new TeacherDetails("anna.ivanova@example.com", "+79991234567")
        );
    }

    private SchoolClassFullResponse buildSchoolClassFullResponse() {
        return new SchoolClassFullResponse(
                SCHOOL_CLASS_ID,
                NAME,
                buildTeacherResponse(),
                TEACHER_ID,
                new BatchUserResponse(List.of(buildStudentResponse()), List.of())
        );
    }

    @Test
    @DisplayName("GET /school-classes/{id} — 200 and school class response")
    void getById_ShouldReturn200() throws Exception {
        when(schoolClassService.findById(SCHOOL_CLASS_ID)).thenReturn(buildSchoolClassResponse());

        mockMvc.perform(get("/api/v1/school-classes/{id}", SCHOOL_CLASS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SCHOOL_CLASS_ID))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.academicYear.id").value(2L))
                .andExpect(jsonPath("$.classTeacherId").value(TEACHER_ID));
    }

    @Test
    @DisplayName("GET /school-classes/{id}/details — 200 and full school class response")
    void getWithDetailsById_ShouldReturn200() throws Exception {
        when(schoolClassService.findWithStudentsById(SCHOOL_CLASS_ID)).thenReturn(buildSchoolClassFullResponse());

        mockMvc.perform(get("/api/v1/school-classes/{id}/details", SCHOOL_CLASS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SCHOOL_CLASS_ID))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.teacher.user.id").value(TEACHER_ID))
                .andExpect(jsonPath("$.classTeacherId").value(TEACHER_ID))
                .andExpect(jsonPath("$.students.found[0].id").value(STUDENT_ID));
    }

    @Test
    @DisplayName("GET /school-classes/search/by-student — 200 and school class response")
    void getByStudentId_ShouldReturn200() throws Exception {
        when(schoolClassService.findByStudent(STUDENT_ID)).thenReturn(buildSchoolClassResponse());

        mockMvc.perform(get("/api/v1/school-classes/search/by-student")
                        .param("studentId", String.valueOf(STUDENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SCHOOL_CLASS_ID))
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    @DisplayName("GET /school-classes — 200 and school class list")
    void findAll_ShouldReturn200() throws Exception {
        when(schoolClassService.findAll()).thenReturn(List.of(buildSchoolClassResponse()));

        mockMvc.perform(get("/api/v1/school-classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SCHOOL_CLASS_ID))
                .andExpect(jsonPath("$[0].name").value(NAME));
    }

    @Test
    @DisplayName("GET /school-classes/unassigned — 200 and unassigned students list")
    void getAllUnassignedStudents_ShouldReturn200() throws Exception {
        when(classStudentService.getUnassignedStudents()).thenReturn(List.of(buildStudentResponse()));

        mockMvc.perform(get("/api/v1/school-classes/unassigned-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(STUDENT_ID))
                .andExpect(jsonPath("$[0].firstName").value("Ivan"));
    }

    @Test
    @DisplayName("POST /school-classes — 200 and create school class")
    void create_ShouldReturn200() throws Exception {
        SchoolClassRequest request = new SchoolClassRequest(NAME, 1L);
        when(schoolClassService.create(request)).thenReturn(buildSchoolClassResponse());

        mockMvc.perform(post("/api/v1/school-classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SCHOOL_CLASS_ID))
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    @DisplayName("PATCH /school-classes/{id} — 200 on update")
    void update_ShouldReturn200() throws Exception {
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest("10B");
        doNothing().when(schoolClassService).update(SCHOOL_CLASS_ID, request);

        mockMvc.perform(patch("/api/v1/school-classes/{id}", SCHOOL_CLASS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(schoolClassService).update(SCHOOL_CLASS_ID, request);
    }

    @Test
    @DisplayName("POST /school-classes/{classId}/students/{studentId} — 200 on add student")
    void addStudent_ShouldReturn200() throws Exception {
        doNothing().when(classStudentService).addStudent(SCHOOL_CLASS_ID, STUDENT_ID);

        mockMvc.perform(post("/api/v1/school-classes/{classId}/students/{studentId}", SCHOOL_CLASS_ID, STUDENT_ID))
                .andExpect(status().isOk());

        verify(classStudentService).addStudent(SCHOOL_CLASS_ID, STUDENT_ID);
    }

    @Test
    @DisplayName("DELETE /school-classes/{classId}/students/{studentId} — 200 on remove student")
    void removeStudent_ShouldReturn200() throws Exception {
        doNothing().when(classStudentService).removeStudent(SCHOOL_CLASS_ID, STUDENT_ID);

        mockMvc.perform(delete("/api/v1/school-classes/{classId}/students/{studentId}", SCHOOL_CLASS_ID, STUDENT_ID))
                .andExpect(status().isOk());

        verify(classStudentService).removeStudent(SCHOOL_CLASS_ID, STUDENT_ID);
    }

    @Test
    @DisplayName("PUT /school-classes/{classId}/assign-teacher/{teacherId} — 200 on assign teacher")
    void assignTeacher_ShouldReturn200() throws Exception {
        doNothing().when(schoolClassService).assignTeacher(SCHOOL_CLASS_ID, TEACHER_ID);

        mockMvc.perform(put("/api/v1/school-classes/{classId}/teacher/{teacherId}", SCHOOL_CLASS_ID, TEACHER_ID))
                .andExpect(status().isOk());

        verify(schoolClassService).assignTeacher(SCHOOL_CLASS_ID, TEACHER_ID);
    }

    @Test
    @DisplayName("DELETE /school-classes/{id} — 200 on delete")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(schoolClassService).delete(SCHOOL_CLASS_ID);

        mockMvc.perform(delete("/api/v1/school-classes/{id}", SCHOOL_CLASS_ID))
                .andExpect(status().isNoContent());

        verify(schoolClassService).delete(SCHOOL_CLASS_ID);
    }
}
