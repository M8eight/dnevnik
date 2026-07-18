package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.AttendanceService;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.web.controller.AttendanceController;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private AttendanceService attendanceService;

    private static final Long ATTENDANCE_ID = 1L;
    private static final Long STUDENT_ID = 10L;
    private static final Long LESSON_INSTANCE_ID = 20L;

    private AttendanceRequest buildRequest() {
        return new AttendanceRequest(
                STUDENT_ID,
                AttendanceStatus.LATE,
                LESSON_INSTANCE_ID
        );
    }

    private AttendanceResponse buildResponse() {
        return new AttendanceResponse(
                ATTENDANCE_ID,
                STUDENT_ID,
                AttendanceStatus.LATE,
                new LessonInstanceDto(LESSON_INSTANCE_ID, LocalDate.of(2025, 1, 10))
        );
    }

    @Test
    @DisplayName("POST /attendances — 200 и созданное посещение")
    void createAttendance_ShouldReturn200() throws Exception {
        AttendanceRequest request = buildRequest();
        when(attendanceService.create(request)).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceId").value(ATTENDANCE_ID))
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.status").value("LATE"))
                .andExpect(jsonPath("$.lessonInstance.id").value(LESSON_INSTANCE_ID))
                .andExpect(jsonPath("$.lessonInstance.lessonDate").value("2025-01-10"));
    }

    @Test
    @DisplayName("POST /attendances — 404 если не найдено занятие")
    void createAttendance_ShouldReturn404_WhenLessonInstanceNotFound() throws Exception {
        AttendanceRequest request = buildRequest();
        doThrow(new NotFoundException("Lesson Instance Not Found " + LESSON_INSTANCE_ID, AcademicExceptionCode.LESSON_INSTANCE_NOT_FOUND))
                .when(attendanceService).create(request);

        mockMvc.perform(post("/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson Instance Not Found " + LESSON_INSTANCE_ID));
    }

    @Test
    @DisplayName("POST /attendances — 409 если академический период закрыт")
    void createAttendance_ShouldReturn409_WhenAcademicPeriodClosed() throws Exception {
        AttendanceRequest request = buildRequest();
        doThrow(new ConflictException("Academic Period is closed", AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT))
                .when(attendanceService).create(request);

        mockMvc.perform(post("/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Academic Period is closed"));
    }

    @Test
    @DisplayName("DELETE /attendances/{id} — 200 при успешном удалении")
    void deleteAttendance_ShouldReturn200() throws Exception {
        doNothing().when(attendanceService).delete(ATTENDANCE_ID);

        mockMvc.perform(delete("/api/v1/attendances/{id}", ATTENDANCE_ID))
                .andExpect(status().isOk());

        verify(attendanceService).delete(ATTENDANCE_ID);
    }

    @Test
    @DisplayName("DELETE /attendances/{id} — 404 если посещение не найдено")
    void deleteAttendance_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Attendance with id " + ATTENDANCE_ID + " not found", AcademicExceptionCode.ACADEMIC_PERIOD_NOT_FOUND))
                .when(attendanceService).delete(ATTENDANCE_ID);

        mockMvc.perform(delete("/api/v1/attendances/{id}", ATTENDANCE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Attendance with id " + ATTENDANCE_ID + " not found"));
    }

    @Test
    @DisplayName("DELETE /attendances/{id} — 409 если удаление запрещено")
    void deleteAttendance_ShouldReturn409_WhenConflict() throws Exception {
        doThrow(new ConflictException("Cannot delete attendance record", AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT))
                .when(attendanceService).delete(ATTENDANCE_ID);

        mockMvc.perform(delete("/api/v1/attendances/{id}", ATTENDANCE_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete attendance record"));
    }
}
