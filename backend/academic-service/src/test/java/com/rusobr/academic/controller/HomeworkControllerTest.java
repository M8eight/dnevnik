package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.HomeworkService;
import com.rusobr.academic.web.controller.HomeworkController;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HomeworkController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HomeworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private HomeworkService homeworkService;

    private static final Long HOMEWORK_ID = 1L;
    private static final Long LESSON_INSTANCE_ID = 100L;
    private static final Long TEACHING_ASSIGNMENT_ID = 200L;
    private static final Long STUDENT_ID = 10L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);

    private HomeworkWithSubjectResponse buildHomeworkWithSubject() {
        return new HomeworkWithSubjectResponse(HOMEWORK_ID, "Read chapter 5", "Mathematics");
    }

    private HomeworkResponse buildHomeworkResponse() {
        return new HomeworkResponse(HOMEWORK_ID, "Read chapter 5", new LessonInstanceDto(LESSON_INSTANCE_ID, DATE));
    }

    private HomeworkRequest buildHomeworkRequest() {
        return new HomeworkRequest("Read chapter 5", LESSON_INSTANCE_ID);
    }

    @Test
    @DisplayName("GET /homeworks/by-date — 200 and list response")
    void getByDate_ShouldReturn200() throws Exception {
        when(homeworkService.getByDate(DATE, STUDENT_ID)).thenReturn(List.of(buildHomeworkWithSubject()));

        mockMvc.perform(get("/api/v1/homeworks/by-date")
                        .param("date", DATE.toString())
                        .param("studentId", String.valueOf(STUDENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(HOMEWORK_ID))
                .andExpect(jsonPath("$[0].text").value("Read chapter 5"))
                .andExpect(jsonPath("$[0].subjectName").value("Mathematics"));
    }

    @Test
    @DisplayName("GET /homeworks/by-assignment — 200 and page response")
    void getByAssignment_ShouldReturn200() throws Exception {
        Page<HomeworkResponse> page = new PageImpl<>(List.of(buildHomeworkResponse()));
        when(homeworkService.getByAssignment(eq(TEACHING_ASSIGNMENT_ID), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/homeworks/by-assignment")
                        .param("teachingAssignmentId", String.valueOf(TEACHING_ASSIGNMENT_ID))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(HOMEWORK_ID))
                .andExpect(jsonPath("$.content[0].text").value("Read chapter 5"))
                .andExpect(jsonPath("$.content[0].lessonInstance.id").value(LESSON_INSTANCE_ID));
    }

    @Test
    @DisplayName("POST /homeworks — 200 and created homework")
    void create_ShouldReturn200() throws Exception {
        HomeworkRequest request = buildHomeworkRequest();
        when(homeworkService.create(request)).thenReturn(buildHomeworkResponse());

        mockMvc.perform(post("/api/v1/homeworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(HOMEWORK_ID))
                .andExpect(jsonPath("$.text").value("Read chapter 5"))
                .andExpect(jsonPath("$.lessonInstance.id").value(LESSON_INSTANCE_ID))
                .andExpect(jsonPath("$.lessonInstance.lessonDate").value(DATE.toString()));
    }

    @Test
    @DisplayName("DELETE /homeworks/{id} — 200 on success")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(homeworkService).delete(HOMEWORK_ID);

        mockMvc.perform(delete("/api/v1/homeworks/{id}", HOMEWORK_ID))
                .andExpect(status().isOk());

        verify(homeworkService).delete(HOMEWORK_ID);
    }

    @Test
    @DisplayName("DELETE /homeworks/{id} — 404 when homework not found")
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Homework with id " + HOMEWORK_ID + " not found"))
                .when(homeworkService).delete(HOMEWORK_ID);

        mockMvc.perform(delete("/api/v1/homeworks/{id}", HOMEWORK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Homework with id " + HOMEWORK_ID + " not found"));
    }
}
