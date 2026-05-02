package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.service.SubjectService;
import com.rusobr.academic.web.controller.SubjectController;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubjectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubjectControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean SubjectService subjectService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/subjects
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/subjects")
    class CreateSubject {

        @Test
        @DisplayName("возвращает 200 и созданный предмет")
        void returns200WithCreatedSubject() throws Exception {
            SubjectRequestDto request = new SubjectRequestDto("Химия");
            SubjectResponseDto response = new SubjectResponseDto(1L, "Химия");

            when(subjectService.createSubject(any(SubjectRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/subjects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Химия"));
        }

        @Test
        @DisplayName("пустое название — возвращает 400")
        void blankName_returns400() throws Exception {
            SubjectRequestDto request = new SubjectRequestDto("");

            mockMvc.perform(post("/api/v1/subjects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(subjectService);
        }

        @Test
        @DisplayName("название длиннее 100 символов — возвращает 400")
        void nameTooLong_returns400() throws Exception {
            SubjectRequestDto request = new SubjectRequestDto("А".repeat(101));

            mockMvc.perform(post("/api/v1/subjects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(subjectService);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/subjects
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/subjects")
    class GetSubjects {

        @Test
        @DisplayName("возвращает 200 и страницу предметов")
        void returns200WithPage() throws Exception {
            SubjectResponseDto dto = new SubjectResponseDto(1L, "Математика");
            PageImpl<SubjectResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

            when(subjectService.getSubjects(any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/subjects")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Математика"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("нет предметов — возвращает 200 и пустую страницу")
        void returns200WithEmptyPage() throws Exception {
            when(subjectService.getSubjects(any())).thenReturn(org.springframework.data.domain.Page.empty());

            mockMvc.perform(get("/api/v1/subjects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/v1/subjects/{id}
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/v1/subjects/{id}")
    class DeleteSubject {

        @Test
        @DisplayName("возвращает 200 при успешном удалении")
        void returns200() throws Exception {
            doNothing().when(subjectService).deleteSubject(1L);

            mockMvc.perform(delete("/api/v1/subjects/1"))
                    .andExpect(status().isOk());

            verify(subjectService).deleteSubject(1L);
        }

        @Test
        @DisplayName("предмет не найден — возвращает 404")
        void notFound_returns404() throws Exception {
            doThrow(new NotFoundException("Subject not found 99"))
                    .when(subjectService).deleteSubject(99L);

            mockMvc.perform(delete("/api/v1/subjects/99"))
                    .andExpect(status().isNotFound());
        }
    }
}