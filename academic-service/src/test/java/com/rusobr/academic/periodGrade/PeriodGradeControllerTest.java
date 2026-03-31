package com.rusobr.academic.periodGrade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.service.PeriodGradeService;
import com.rusobr.academic.web.controller.GradeController;
import com.rusobr.academic.web.controller.PeriodGradeController;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeResponse;
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

@WebMvcTest(PeriodGradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PeriodGradeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PeriodGradeService periodGradeService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("GET /api/v1/period/grades/class")
    class FindBySchoolClassId {

        @Test
        @DisplayName("возвращает 200 и список студентов с оценками")
        void returns200WithList() throws Exception {
            List<StudentPeriodGradeResponse> response = List.of(
                    new StudentPeriodGradeResponse(1L, "Иван", "Иванов", 5, "Отлично", 10L),
                    new StudentPeriodGradeResponse(2L, "Мария", "Петрова", null, null, null)
            );

            when(periodGradeService.findBySchoolClassId(1L, 3L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/period/grades/class")
                            .param("teachingAssignmentId", "1")
                            .param("academicPeriodId", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].studentId").value(1))
                    .andExpect(jsonPath("$[0].firstName").value("Иван"))
                    .andExpect(jsonPath("$[0].value").value(5))
                    .andExpect(jsonPath("$[1].value").isEmpty());
        }

        @Test
        @DisplayName("возвращает 200 и пустой список")
        void returns200WithEmptyList() throws Exception {
            when(periodGradeService.findBySchoolClassId(1L, 3L)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/period/grades/class")
                            .param("teachingAssignmentId", "1")
                            .param("academicPeriodId", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/period/grades")
    class CreatePeriodGrade {

        @Test
        @DisplayName("возвращает 201 и созданную оценку")
        void returns201WithCreatedGrade() throws Exception {
            PeriodGradeRequest request = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));
            PeriodGradeResponse response = new PeriodGradeResponse(10L, 5, "Отлично", 1L);

            when(periodGradeService.createGrade(any(PeriodGradeRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/period/grades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.value").value(5))
                    .andExpect(jsonPath("$.description").value("Отлично"));
        }

        @Test
        @DisplayName("период закрыт — возвращает 409")
        void periodClosed_returns409() throws Exception {
            PeriodGradeRequest request = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));

            when(periodGradeService.createGrade(any())).thenThrow(new ConflictException("Period is already closed"));

            mockMvc.perform(post("/api/v1/period/grades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("период не найден — возвращает 404")
        void periodNotFound_returns404() throws Exception {
            PeriodGradeRequest request = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));

            when(periodGradeService.createGrade(any())).thenThrow(new NotFoundException("Academic period not found"));

            mockMvc.perform(post("/api/v1/period/grades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/period/grades/{id}")
    class DeletePeriodGrade {

        @Test
        @DisplayName("возвращает 204 при успешном удалении")
        void returns204() throws Exception {
            doNothing().when(periodGradeService).deletePeriodGrade(1L);

            mockMvc.perform(delete("/api/v1/period/grades/1"))
                    .andExpect(status().isNoContent());

            verify(periodGradeService).deletePeriodGrade(1L);
        }

        @Test
        @DisplayName("оценка не найдена — возвращает 404")
        void notFound_returns404() throws Exception {
            doThrow(new NotFoundException("Period grade not found"))
                    .when(periodGradeService).deletePeriodGrade(1L);

            mockMvc.perform(delete("/api/v1/period/grades/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("период закрыт — возвращает 409")
        void periodClosed_returns409() throws Exception {
            doThrow(new ConflictException("Period is already closed"))
                    .when(periodGradeService).deletePeriodGrade(1L);

            mockMvc.perform(delete("/api/v1/period/grades/1"))
                    .andExpect(status().isConflict());
        }
    }
}
