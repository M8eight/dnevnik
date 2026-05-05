package com.rusobr.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.service.student.StudentService;
import com.rusobr.user.web.controller.StudentController;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private StudentService studentService;

    // ─────────────────────────────────────────────
    // POST /api/v1/students/batch
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /batch — 200 OK и список студентов")
    void findBatchStudents_ShouldReturnList() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        List<StudentResponse> response = List.of(
                new StudentResponse(1L, "Иван", "Иванов", "kc-1"),
                new StudentResponse(2L, "Мария", "Петрова", "kc-2")
        );

        when(studentService.findSimpleBatchStudents(ids)).thenReturn(response);

        mockMvc.perform(post("/api/v1/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Иван"))
                .andExpect(jsonPath("$[1].lastName").value("Петрова"));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/students/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /{id} — 200 OK и детальная информация")
    void findById_ShouldReturnDetail() throws Exception {
        SchoolClassResponse schoolClass = new SchoolClassResponse(10L, "10А", "2025-2026", 5L);
        TeacherResponse teacher = new TeacherResponse(5L, "kc-t", "Анна", "Смирнова", "123", "a@a.ru");

        StudentResponseDetail detail = new StudentResponseDetail(
                1L, 100L, "kc-student", "Иван", "Иванов", "IT", schoolClass, teacher
        );

        when(studentService.findStudentDetailById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studyProfile").value("IT"))
                // Проверяем вложенный класс
                .andExpect(jsonPath("$.schoolClass.name").value("10А"))
                // Проверяем вложенного учителя
                .andExpect(jsonPath("$.schoolClassTeacher.firstName").value("Анна"));
    }

    @Test
    @DisplayName("GET /{id} — 404 если студент не найден")
    void findById_ShouldReturn404() throws Exception {
        when(studentService.findStudentDetailById(99L))
                .thenThrow(new NotFoundException("Student not found"));

        mockMvc.perform(get("/api/v1/students/99"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/students/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /{id} — 200 OK")
    void deleteById_ShouldReturn200() throws Exception {
        doNothing().when(studentService).deleteStudentById(1L);

        mockMvc.perform(delete("/api/v1/students/1"))
                .andExpect(status().isOk());

        verify(studentService).deleteStudentById(1L);
    }

    @Test
    @DisplayName("DELETE /{id} — 404 если студент для удаления не найден")
    void deleteById_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Student not found"))
                .when(studentService).deleteStudentById(99L);

        mockMvc.perform(delete("/api/v1/students/99"))
                .andExpect(status().isNotFound());
    }
}