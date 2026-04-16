package com.rusobr.academic.controller;

import com.rusobr.academic.infrastructure.service.LessonInstanceService;
import com.rusobr.academic.web.controller.LessonInstanceController;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryLessonResponse;
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

@WebMvcTest(LessonInstanceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LessonInstanceControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean LessonInstanceService lessonInstanceService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("GET /api/v1/schedule")
    class GetScheduleByStudentIdAndDatePeriod {

        @Test
        @DisplayName("возвращает 200 и дневник, сгруппированный по датам")
        void returns200WithDiary() throws Exception {
            LocalDate monday = LocalDate.of(2026, 4, 13);
            LocalDate tuesday = LocalDate.of(2026, 4, 14);

            DiaryLessonResponse lesson1 = new DiaryLessonResponse(monday, "Математика", DayOfWeek.MONDAY, 1, "101", List.of(), List.of(), null);
            DiaryLessonResponse lesson2 = new DiaryLessonResponse(tuesday, "Физика", DayOfWeek.TUESDAY, 2, "202", List.of(), List.of(), null);

            when(lessonInstanceService.getDiaryLessonsByStudentIdAndDateRange(1L, monday, tuesday))
                    .thenReturn(Map.of(
                            monday, List.of(lesson1),
                            tuesday, List.of(lesson2)
                    ));

            mockMvc.perform(get("/api/v1/schedule")
                            .param("studentId", "1")
                            .param("startDate", "2026-04-13")
                            .param("endDate", "2026-04-14"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$['2026-04-13'].length()").value(1))
                    .andExpect(jsonPath("$['2026-04-13'][0].subjectName").value("Математика"))
                    .andExpect(jsonPath("$['2026-04-14'][0].subjectName").value("Физика"));
        }

        @Test
        @DisplayName("нет уроков в диапазоне — возвращает 200 и пустой объект")
        void returns200WithEmptyMap() throws Exception {
            LocalDate start = LocalDate.of(2026, 4, 13);
            LocalDate end = LocalDate.of(2026, 4, 19);

            when(lessonInstanceService.getDiaryLessonsByStudentIdAndDateRange(1L, start, end))
                    .thenReturn(Map.of());

            mockMvc.perform(get("/api/v1/schedule")
                            .param("studentId", "1")
                            .param("startDate", "2026-04-13")
                            .param("endDate", "2026-04-19"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("несколько уроков в один день — все попадают в список")
        void multipleLessonsOnSameDate() throws Exception {
            LocalDate monday = LocalDate.of(2026, 4, 13);

            DiaryLessonResponse lesson1 = new DiaryLessonResponse(monday, "Математика", DayOfWeek.MONDAY, 1, "101", List.of(), List.of(), null);
            DiaryLessonResponse lesson2 = new DiaryLessonResponse(monday, "Физика", DayOfWeek.MONDAY, 2, "202", List.of(), List.of(), null);

            when(lessonInstanceService.getDiaryLessonsByStudentIdAndDateRange(1L, monday, monday))
                    .thenReturn(Map.of(monday, List.of(lesson1, lesson2)));

            mockMvc.perform(get("/api/v1/schedule")
                            .param("studentId", "1")
                            .param("startDate", "2026-04-13")
                            .param("endDate", "2026-04-13"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$['2026-04-13'].length()").value(2))
                    .andExpect(jsonPath("$['2026-04-13'][0].subjectName").value("Математика"))
                    .andExpect(jsonPath("$['2026-04-13'][1].subjectName").value("Физика"));
        }
    }
}