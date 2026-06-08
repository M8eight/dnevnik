package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.ScheduleService;
import com.rusobr.academic.web.controller.ScheduleController;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryScheduleDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonRequest;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private ScheduleService scheduleService;

    private static final Long STUDENT_ID = 42L;
    private static final Long CLASS_ID = 11L;
    private static final Long SCHEDULE_ID = 21L;
    private static final Long SUBJECT_ID = 31L;
    private static final Long TEACHER_ID = 41L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 10);

    private ScheduleLessonResponse buildScheduleLessonResponse() {
        return new ScheduleLessonResponse(SCHEDULE_ID, 2, "Mathematics", "101");
    }

    private DiaryScheduleDto buildDiaryScheduleDto() {
        return new DiaryScheduleDto(
                SCHEDULE_ID,
                DayOfWeek.MONDAY,
                2,
                "101",
                DATE,
                DATE.plusWeeks(1),
                new SubjectResponseDto(SUBJECT_ID, "Mathematics"),
                null
        );
    }

    private SchoolLessonResponse buildSchoolLessonResponse() {
        return new SchoolLessonResponse(SCHEDULE_ID, 2, "Mathematics", "101", DayOfWeek.MONDAY);
    }

    private ScheduleLessonDto buildScheduleLessonDto() {
        return new ScheduleLessonDto(
                SCHEDULE_ID,
                DayOfWeek.MONDAY,
                2,
                "101",
                DATE,
                DATE.plusWeeks(1),
                new SubjectResponseDto(SUBJECT_ID, "Mathematics"),
                new UserFeignResponse(TEACHER_ID, "Ivan", "Petrov", "ipetrov", "keycloak-1")
        );
    }

    private ScheduleLessonRequest buildScheduleLessonRequest() {
        return new ScheduleLessonRequest(
                CLASS_ID,
                SUBJECT_ID,
                TEACHER_ID,
                DayOfWeek.MONDAY,
                2,
                "101",
                DATE
        );
    }

    @Test
    @DisplayName("GET /schedule/by-date — 200 and schedule list")
    void getScheduleByDate_ShouldReturn200() throws Exception {
        when(scheduleService.getByDate(STUDENT_ID, DayOfWeek.MONDAY, DATE))
                .thenReturn(List.of(buildScheduleLessonResponse()));

        mockMvc.perform(get("/api/v1/schedule/by-date")
                        .param("studentId", String.valueOf(STUDENT_ID))
                        .param("dayOfWeek", DayOfWeek.MONDAY.name())
                        .param("date", DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SCHEDULE_ID))
                .andExpect(jsonPath("$[0].lessonNumber").value(2))
                .andExpect(jsonPath("$[0].subjectName").value("Mathematics"));
    }

    @Test
    @DisplayName("GET /schedules/diary — 200 and diary schedule list")
    void getDiaryScheduleByStudentId_ShouldReturn200() throws Exception {
        when(scheduleService.getByStudentId(STUDENT_ID, DATE, DATE.plusDays(1)))
                .thenReturn(List.of(buildDiaryScheduleDto()));

        mockMvc.perform(get("/api/v1/schedules/diary")
                        .param("studentId", String.valueOf(STUDENT_ID))
                        .param("startDate", DATE.toString())
                        .param("endDate", DATE.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SCHEDULE_ID))
                .andExpect(jsonPath("$[0].subject.name").value("Mathematics"));
    }

    @Test
    @DisplayName("GET /schedules/by-student — 200 and weekly schedule map")
    void getWeekSchedule_ShouldReturn200() throws Exception {
        when(scheduleService.getWeekSchedule(STUDENT_ID))
                .thenReturn(Map.of(DayOfWeek.MONDAY, List.of(buildSchoolLessonResponse())));

        mockMvc.perform(get("/api/v1/schedules/by-student")
                        .param("studentId", String.valueOf(STUDENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MONDAY[0].id").value(SCHEDULE_ID))
                .andExpect(jsonPath("$.MONDAY[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    @DisplayName("GET /schedules/by-class — 200 and class schedule map")
    void getClassSchedule_ShouldReturn200() throws Exception {
        when(scheduleService.getByClass(CLASS_ID, DATE))
                .thenReturn(Map.of(DayOfWeek.MONDAY, List.of(buildScheduleLessonDto())));

        mockMvc.perform(get("/api/v1/schedules/by-class")
                        .param("classId", String.valueOf(CLASS_ID))
                        .param("date", DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MONDAY[0].id").value(SCHEDULE_ID))
                .andExpect(jsonPath("$.MONDAY[0].subject.name").value("Mathematics"));
    }

    @Test
    @DisplayName("POST /schedules — 200 and create schedule")
    void create_ShouldReturn200() throws Exception {
        ScheduleLessonRequest request = buildScheduleLessonRequest();
        doNothing().when(scheduleService).create(request);

        mockMvc.perform(post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(scheduleService).create(request);
    }

    @Test
    @DisplayName("PATCH /schedules/{scheduleId}/close — 200 on close")
    void close_ShouldReturn200() throws Exception {
        doNothing().when(scheduleService).close(SCHEDULE_ID, DATE);

        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/close", SCHEDULE_ID)
                        .param("closeDate", DATE.toString()))
                .andExpect(status().isOk());

        verify(scheduleService).close(SCHEDULE_ID, DATE);
    }

    @Test
    @DisplayName("PATCH /schedules/load — 200 on load")
    void load_ShouldReturn200() throws Exception {
        LocalDate toDate = DATE.plusDays(7);
        doNothing().when(scheduleService).load(CLASS_ID, DATE, toDate);

        mockMvc.perform(patch("/api/v1/schedules/load")
                        .param("classId", String.valueOf(CLASS_ID))
                        .param("fromDate", DATE.toString())
                        .param("toDate", toDate.toString()))
                .andExpect(status().isOk());

        verify(scheduleService).load(CLASS_ID, DATE, toDate);
    }
}
