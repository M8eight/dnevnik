package com.rusobr.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.infrastructure.service.TeacherService;
import com.rusobr.academic.web.controller.TeacherController;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    @Test
    @DisplayName("Должен вернуть список студентов в классе")
    void shouldReturnUsers() throws Exception {
        List<UserResponse> users = List.of(
                new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L),
                new UserResponse("Алексей", "Кочетыгов", "abc-123", 2L)
        );

        when(teacherService.getUsersIdFromClass(any())).thenReturn(users);

        mockMvc.perform(get("/api/v1/class/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Алексей"));
    }

    @Test
    @DisplayName("Должен вернуть данные журнала с оценками")
    void shouldReturnGradeJournal() throws Exception {
        // Подготовка данных
        GradeJournalResponse response = new GradeJournalResponse(
                List.of(new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L)),
                List.of(LocalDate.of(2025, 9, 1)),
                List.of(), // пустые оценки для краткости
                null // DTO периода
        );

        when(teacherService.getClassGrades(any(), any())).thenReturn(response);

        // Выполняем запрос с параметром даты
        mockMvc.perform(get("/api/v1/class/grades/1")
                        .param("date", "2025-09-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].firstName").value("Алексей"))
                .andExpect(jsonPath("$.dates[0]").value("2025-09-01"));
    }
}
