package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.TeacherSubjectService;
import com.rusobr.academic.web.controller.TeacherSubjectController;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectRequest;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectResponse;
import jakarta.validation.constraints.NotNull;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeacherSubjectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TeacherSubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private TeacherSubjectService teacherSubjectService;

    private static final Long TEACHER_ID = 11L;
    private static final Long SUBJECT_ID = 21L;

    private UserFeignResponse buildTeacher() {
        return new UserFeignResponse(TEACHER_ID, "Anna", "Ivanova", "aivanova", "keycloak-2");
    }

    private SubjectResponseDto buildSubject() {
        return new SubjectResponseDto(SUBJECT_ID, "Mathematics");
    }

    private TeacherSubjectResponse buildResponse() {
        return new TeacherSubjectResponse(buildTeacher(), buildSubject());
    }

    @Test
    @DisplayName("GET /teacher-subjects — 200 and list")
    void findAll_ShouldReturn200() throws Exception {
        when(teacherSubjectService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/teacher-subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teacher.id").value(TEACHER_ID))
                .andExpect(jsonPath("$[0].subject.id").value(SUBJECT_ID));
    }

    @Test
    @DisplayName("POST /teacher-subjects — 200 and create")
    void create_ShouldReturn200() throws Exception {
        TeacherSubjectRequest request = new TeacherSubjectRequest(TEACHER_ID, SUBJECT_ID);
        when(teacherSubjectService.create(request)).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/teacher-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacher.id").value(TEACHER_ID));
    }

    @Test
    @DisplayName("DELETE /teacher-subjects — 200 on delete")
    void delete_ShouldReturn200() throws Exception {
        TeacherSubjectRequest request = new TeacherSubjectRequest(TEACHER_ID, SUBJECT_ID);
        doNothing().when(teacherSubjectService).delete(request);

        mockMvc.perform(delete("/api/v1/teacher-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(teacherSubjectService).delete(request);
    }
}
