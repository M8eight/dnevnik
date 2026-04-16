package com.rusobr.academic.controller;

import com.rusobr.academic.infrastructure.service.HomeworkService;
import com.rusobr.academic.web.controller.HomeworkController;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeworkController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HomeworkControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean HomeworkService homeworkService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("GET /api/v1/homeworks/by-date")
    class GetHomeworksByDate {

        @Test
        @DisplayName("возвращает 200 и список домашних заданий")
        void returns200WithList() throws Exception {
            HomeworkResponse hw1 = new HomeworkResponse(1L, "Стр. 42, упр. 5", "Математика");
            HomeworkResponse hw2 = new HomeworkResponse(2L, "Параграф 10", "Физика");

            when(homeworkService.getHomeworksByDate(
                    java.time.LocalDate.of(2026, 4, 14), 1L))
                    .thenReturn(List.of(hw1, hw2));

            mockMvc.perform(get("/api/v1/homeworks/by-date")
                            .param("date", "2026-04-14")
                            .param("studentId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].text").value("Стр. 42, упр. 5"))
                    .andExpect(jsonPath("$[0].subjectName").value("Математика"))
                    .andExpect(jsonPath("$[1].subjectName").value("Физика"));
        }

        @Test
        @DisplayName("нет заданий на дату — возвращает 200 и пустой список")
        void returns200WithEmptyList() throws Exception {
            when(homeworkService.getHomeworksByDate(
                    java.time.LocalDate.of(2026, 4, 14), 1L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/homeworks/by-date")
                            .param("date", "2026-04-14")
                            .param("studentId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}