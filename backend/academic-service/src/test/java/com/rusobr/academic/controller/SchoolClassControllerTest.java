package com.rusobr.academic.controller;

import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.service.SchoolClassService;
import com.rusobr.academic.web.controller.SchoolClassController;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchoolClassController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SchoolClassControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean SchoolClassService schoolClassService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("GET /api/v1/school-classes/{id}")
    class GetSchoolClassById {

        @Test
        @DisplayName("возвращает 200 и данные класса")
        void returns200WithSchoolClass() throws Exception {
            SchoolClassResponse response = new SchoolClassResponse(1L, "10А", "2025-2026", 5L);

            when(schoolClassService.findById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/school-classes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("10А"))
                    .andExpect(jsonPath("$.year").value("2025-2026"))
                    .andExpect(jsonPath("$.classTeacherId").value(5));
        }

        @Test
        @DisplayName("класс не найден — возвращает 404")
        void notFound_returns404() throws Exception {
            when(schoolClassService.findById(99L)).thenThrow(new NotFoundException("SchoolClass Not Found by id: 99"));

            mockMvc.perform(get("/api/v1/school-classes/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/school-classes/search/by-student")
    class GetSchoolClassByStudentId {

        @Test
        @DisplayName("возвращает 200 и данные класса студента")
        void returns200WithSchoolClass() throws Exception {
            SchoolClassResponse response = new SchoolClassResponse(1L, "10А", "2025-2026", 5L);

            when(schoolClassService.findClassByStudentId(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/school-classes/search/by-student")
                            .param("studentId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("10А"));
        }

        @Test
        @DisplayName("студент не привязан к классу — возвращает 200 и null тело")
        void studentNotInClass_returns200WithNullBody() throws Exception {
            when(schoolClassService.findClassByStudentId(99L)).thenReturn(null);

            mockMvc.perform(get("/api/v1/school-classes/search/by-student")
                            .param("studentId", "99"))
                    .andExpect(status().isOk());
        }
    }
}