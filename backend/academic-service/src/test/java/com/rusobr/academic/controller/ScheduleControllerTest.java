package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.infrastructure.service.ScheduleService;
import com.rusobr.academic.web.controller.ScheduleController;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekItemDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ScheduleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ScheduleService scheduleService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("GET /api/v1/schedule/by-date")
    class GetScheduleByDate {

        @Test
        @DisplayName("возвращает 200 и список уроков на дату")
        void returns200WithList() throws Exception {
            LocalDate date = LocalDate.of(2026, 4, 14);
            ScheduleLessonResponse response = new ScheduleLessonResponse(1L, 2, "Физика", "202");

            when(scheduleService.getScheduleByDate(1L, DayOfWeek.TUESDAY, date)).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/schedule/by-date")
                            .param("studentId", "1")
                            .param("dayOfWeek", "TUESDAY")
                            .param("date", "2026-04-14"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].subjectName").value("Физика"))
                    .andExpect(jsonPath("$[0].classRoom").value("202"));
        }

        @Test
        @DisplayName("нет уроков на дату — возвращает 200 и пустой список")
        void returns200WithEmptyList() throws Exception {
            LocalDate date = LocalDate.of(2026, 4, 14);

            when(scheduleService.getScheduleByDate(1L, DayOfWeek.TUESDAY, date)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/schedule/by-date")
                            .param("studentId", "1")
                            .param("dayOfWeek", "TUESDAY")
                            .param("date", "2026-04-14"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/schedules/by-student")
    class GetWeekSchedule {

        @Test
        @DisplayName("возвращает 200 и расписание на неделю, сгруппированное по дням")
        void returns200WithWeekSchedule() throws Exception {
            SchoolLessonResponse mon = new SchoolLessonResponse(1L, 1, "Математика", "101", DayOfWeek.MONDAY);
            SchoolLessonResponse tue = new SchoolLessonResponse(2L, 2, "Физика", "202", DayOfWeek.TUESDAY);

            when(scheduleService.getWeekSchedule(1L)).thenReturn(Map.of(
                    DayOfWeek.MONDAY, List.of(mon),
                    DayOfWeek.TUESDAY, List.of(tue)
            ));

            mockMvc.perform(get("/api/v1/schedules/by-student")
                            .param("studentId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.MONDAY.length()").value(1))
                    .andExpect(jsonPath("$.MONDAY[0].subjectName").value("Математика"))
                    .andExpect(jsonPath("$.TUESDAY.length()").value(1))
                    .andExpect(jsonPath("$.TUESDAY[0].subjectName").value("Физика"));
        }

        @Test
        @DisplayName("нет уроков — возвращает 200 и пустой объект")
        void returns200WithEmptyMap() throws Exception {
            when(scheduleService.getWeekSchedule(1L)).thenReturn(Map.of());

            mockMvc.perform(get("/api/v1/schedules/by-student")
                            .param("studentId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}