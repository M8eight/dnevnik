package com.rusobr.user.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.infrastructure.service.StudentService;
import com.rusobr.user.web.controller.StudentController;
import com.rusobr.user.web.dto.student.StudentResponse;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllStudentsByIds() throws Exception {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<StudentResponse> res = List.of(
                new StudentResponse(1L, "Алексей", "Кочетыгов"),
                new StudentResponse(2L, "Андрей", "Малахов"),
                new StudentResponse(3L, "Михаил", "Баварский")
        );

        when(studentService.findBatchStudents(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/students/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

}
