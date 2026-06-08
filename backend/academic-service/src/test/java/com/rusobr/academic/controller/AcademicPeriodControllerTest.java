package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.AcademicPeriodService;
import com.rusobr.academic.web.controller.AcademicPeriodController;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AcademicPeriodController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AcademicPeriodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private AcademicPeriodService academicPeriodService;

    private static final Long PERIOD_ID = 1L;

    private AcademicPeriodResponse buildResponse() {
        return new AcademicPeriodResponse(
                PERIOD_ID,
                "Q1",
                "2024-2025",
                false,
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private AcademicPeriodRequest buildRequest() {
        return new AcademicPeriodRequest(
                "Q1",
                "2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/academic-periods
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /academic-periods — 200 и список периодов")
    void getAll_ShouldReturn200() throws Exception {
        when(academicPeriodService.getAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/academic-periods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PERIOD_ID))
                .andExpect(jsonPath("$[0].name").value("Q1"))
                .andExpect(jsonPath("$[0].schoolYear").value("2024-2025"))
                .andExpect(jsonPath("$[0].isClosed").value(false))
                .andExpect(jsonPath("$[0].startDate").value("2024-09-01"))
                .andExpect(jsonPath("$[0].endDate").value("2024-12-31"));
    }

    @Test
    @DisplayName("GET /academic-periods — 200 пустой список если периодов нет")
    void getAll_ShouldReturnEmptyList_WhenNoPeriods() throws Exception {
        when(academicPeriodService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/academic-periods"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/academic-periods/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /academic-periods/{id} — 200 и тело ответа")
    void getById_ShouldReturn200() throws Exception {
        when(academicPeriodService.findById(PERIOD_ID)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/academic-periods/{id}", PERIOD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERIOD_ID))
                .andExpect(jsonPath("$.name").value("Q1"))
                .andExpect(jsonPath("$.schoolYear").value("2024-2025"))
                .andExpect(jsonPath("$.startDate").value("2024-09-01"))
                .andExpect(jsonPath("$.endDate").value("2024-12-31"));
    }

    @Test
    @DisplayName("GET /academic-periods/{id} — 404 если период не найден")
    void getById_ShouldReturn404_WhenNotFound() throws Exception {
        when(academicPeriodService.findById(PERIOD_ID))
                .thenThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found"));

        mockMvc.perform(get("/api/v1/academic-periods/{id}", PERIOD_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Academic period with id " + PERIOD_ID + " not found"));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/academic-periods/{id}/open
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /academic-periods/{id}/open — 200 при успешном открытии")
    void openPeriod_ShouldReturn200() throws Exception {
        doNothing().when(academicPeriodService).openPeriod(PERIOD_ID);

        mockMvc.perform(patch("/api/v1/academic-periods/{id}/open", PERIOD_ID))
                .andExpect(status().isOk());

        verify(academicPeriodService).openPeriod(PERIOD_ID);
    }

    @Test
    @DisplayName("PATCH /academic-periods/{id}/open — 404 если период не найден")
    void openPeriod_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found"))
                .when(academicPeriodService).openPeriod(PERIOD_ID);

        mockMvc.perform(patch("/api/v1/academic-periods/{id}/open", PERIOD_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Academic period with id " + PERIOD_ID + " not found"));
    }

    @Test
    @DisplayName("PATCH /academic-periods/{id}/open — 409 если период уже открыт")
    void openPeriod_ShouldReturn409_WhenAlreadyOpen() throws Exception {
        doThrow(new ConflictException("Academic period is already open"))
                .when(academicPeriodService).openPeriod(PERIOD_ID);

        mockMvc.perform(patch("/api/v1/academic-periods/{id}/open", PERIOD_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Academic period is already open"));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/academic-periods/{id}/close
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /academic-periods/{id}/close — 200 при успешном закрытии")
    void closePeriod_ShouldReturn200() throws Exception {
        doNothing().when(academicPeriodService).closePeriod(PERIOD_ID);

        mockMvc.perform(patch("/api/v1/academic-periods/{id}/close", PERIOD_ID))
                .andExpect(status().isOk());

        verify(academicPeriodService).closePeriod(PERIOD_ID);
    }

    @Test
    @DisplayName("PATCH /academic-periods/{id}/close — 404 если период не найден")
    void closePeriod_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found"))
                .when(academicPeriodService).closePeriod(PERIOD_ID);

        mockMvc.perform(patch("/api/v1/academic-periods/{id}/close", PERIOD_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Academic period with id " + PERIOD_ID + " not found"));
    }

    @Test
    @DisplayName("PATCH /academic-periods/{id}/close — 409 если период уже закрыт")
    void closePeriod_ShouldReturn409_WhenAlreadyClosed() throws Exception {
        doThrow(new ConflictException("Academic period is already closed"))
                .when(academicPeriodService).closePeriod(PERIOD_ID);

        mockMvc.perform(patch("/api/v1/academic-periods/{id}/close", PERIOD_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Academic period is already closed"));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/academic-periods
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /academic-periods — 200 и созданный период")
    void create_ShouldReturn200() throws Exception {
        AcademicPeriodRequest request = buildRequest();
        when(academicPeriodService.create(request)).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/academic-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERIOD_ID))
                .andExpect(jsonPath("$.name").value("Q1"))
                .andExpect(jsonPath("$.schoolYear").value("2024-2025"))
                .andExpect(jsonPath("$.isClosed").value(false));
    }

    @Test
    @DisplayName("POST /academic-periods — 409 если startDate после endDate")
    void create_ShouldReturn409_WhenStartDateAfterEndDate() throws Exception {
        AcademicPeriodRequest request = new AcademicPeriodRequest(
                "Q1",
                "2024-2025",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2024, 9, 1)
        );
        doThrow(new ConflictException("Start date cannot be after end date"))
                .when(academicPeriodService).create(request);

        mockMvc.perform(post("/api/v1/academic-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/academic-periods/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /academic-periods/{id} — 200 при успешном удалении")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(academicPeriodService).delete(PERIOD_ID);

        mockMvc.perform(delete("/api/v1/academic-periods/{id}", PERIOD_ID))
                .andExpect(status().isOk());

        verify(academicPeriodService).delete(PERIOD_ID);
    }

    @Test
    @DisplayName("DELETE /academic-periods/{id} — 404 если период не найден")
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found"))
                .when(academicPeriodService).delete(PERIOD_ID);

        mockMvc.perform(delete("/api/v1/academic-periods/{id}", PERIOD_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Academic period with id " + PERIOD_ID + " not found"));
    }

    @Test
    @DisplayName("DELETE /academic-periods/{id} — 409 если период открыт")
    void delete_ShouldReturn409_WhenPeriodIsOpen() throws Exception {
        doThrow(new ConflictException("Cannot delete an open academic period"))
                .when(academicPeriodService).delete(PERIOD_ID);

        mockMvc.perform(delete("/api/v1/academic-periods/{id}", PERIOD_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete an open academic period"));
    }
}