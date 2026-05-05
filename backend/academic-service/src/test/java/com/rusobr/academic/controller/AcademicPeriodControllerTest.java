package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.service.AcademicPeriodService;
import com.rusobr.academic.web.controller.AcademicPeriodController;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcademicPeriodController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AcademicPeriodControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AcademicPeriodService academicPeriodService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/academic-periods/{id}
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/academic-periods/{id}")
    class GetAcademicPeriodById {

        @Test
        @DisplayName("возвращает 200 и период")
        void returns200WithPeriod() throws Exception {
            AcademicPeriodResponse dto = new AcademicPeriodResponse(
                    1L, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodService.findById(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/v1/academic-periods/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Q1"))
                    .andExpect(jsonPath("$.schoolYear").value("2025-2026"))
                    .andExpect(jsonPath("$.isClosed").value(false));
        }

        @Test
        @DisplayName("период не найден — возвращает 404")
        void notFound_returns404() throws Exception {
            when(academicPeriodService.findById(99L))
                    .thenThrow(new NotFoundException("Academic period with id 99 not found!"));

            mockMvc.perform(get("/api/v1/academic-periods/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/academic-periods
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/academic-periods")
    class GetAcademicPeriods {

        @Test
        @DisplayName("возвращает 200 и список периодов")
        void returns200WithList() throws Exception {
            AcademicPeriodResponse dto = new AcademicPeriodResponse(
                    1L, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodService.getAcademicPeriods()).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/v1/academic-periods"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Q1"));
        }

        @Test
        @DisplayName("нет периодов — возвращает 200 и пустой список")
        void returns200WithEmptyList() throws Exception {
            when(academicPeriodService.getAcademicPeriods()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/academic-periods"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/academic-periods/{id}/open
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/academic-periods/{id}/open")
    class OpenPeriod {

        @Test
        @DisplayName("возвращает 200 при успешном открытии")
        void returns200() throws Exception {
            doNothing().when(academicPeriodService).openPeriod(1L);

            mockMvc.perform(post("/api/v1/academic-periods/1/open"))
                    .andExpect(status().isOk());

            verify(academicPeriodService).openPeriod(1L);
        }

        @Test
        @DisplayName("период не найден — возвращает 404")
        void notFound_returns404() throws Exception {
            doThrow(new NotFoundException("Academic period with id 99 not found"))
                    .when(academicPeriodService).openPeriod(99L);

            mockMvc.perform(post("/api/v1/academic-periods/99/open"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/academic-periods/{id}/close
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/academic-periods/{id}/close")
    class ClosePeriod {

        @Test
        @DisplayName("возвращает 200 при успешном закрытии")
        void returns200() throws Exception {
            doNothing().when(academicPeriodService).closePeriod(1L);

            mockMvc.perform(post("/api/v1/academic-periods/1/close"))
                    .andExpect(status().isOk());

            verify(academicPeriodService).closePeriod(1L);
        }

        @Test
        @DisplayName("период не найден — возвращает 404")
        void notFound_returns404() throws Exception {
            doThrow(new NotFoundException("Academic period with id 99 not found"))
                    .when(academicPeriodService).closePeriod(99L);

            mockMvc.perform(post("/api/v1/academic-periods/99/close"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/academic-periods
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/academic-periods")
    class CreateAcademicPeriod {

        @Test
        @DisplayName("возвращает 200 и созданный период")
        void returns200WithCreatedPeriod() throws Exception {
            AcademicPeriodRequest request = new AcademicPeriodRequest(
                    "Q1", "2025-2026",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));
            AcademicPeriodResponse response = new AcademicPeriodResponse(
                    1L, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodService.createAcademicPeriod(any(AcademicPeriodRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/academic-periods")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Q1"))
                    .andExpect(jsonPath("$.schoolYear").value("2025-2026"))
                    .andExpect(jsonPath("$.isClosed").value(false));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/v1/academic-periods/{id}
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/v1/academic-periods/{id}")
    class DeleteAcademicPeriod {

        @Test
        @DisplayName("возвращает 200 при успешном удалении")
        void returns200() throws Exception {
            doNothing().when(academicPeriodService).deleteById(1L);

            mockMvc.perform(delete("/api/v1/academic-periods/1"))
                    .andExpect(status().isOk());

            verify(academicPeriodService).deleteById(1L);
        }

        @Test
        @DisplayName("период не найден — возвращает 404")
        void notFound_returns404() throws Exception {
            doThrow(new NotFoundException("Academic Period not found"))
                    .when(academicPeriodService).deleteById(99L);

            mockMvc.perform(delete("/api/v1/academic-periods/99"))
                    .andExpect(status().isNotFound());
        }
    }
}