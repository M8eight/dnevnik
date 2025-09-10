package com.rusobr.grade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.grade.domain.model.GradeConstant;
import com.rusobr.grade.domain.service.GradeConstantService;
import com.rusobr.grade.web.GradeConstantController;
import com.rusobr.grade.web.GradeController;
import com.rusobr.grade.web.dto.gradeConstant.CreateGradeConstantRequestDto;
import com.rusobr.grade.web.dto.gradeConstant.UpdateGradeConstantDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GradeConstantController.class)
public class GradeConstantRestTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GradeConstantService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllGrades() throws Exception {
        when(service.getAllGradeConstants()).thenReturn(List.of());

        mockMvc.perform(get("/api/grades"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPostGrade() throws Exception {
        GradeConstant gradeConstant = GradeConstant.builder()
                .value(6)
                .name("6 оценка")
                .description("6")
                .build();

        CreateGradeConstantRequestDto createGradeConstantRequest = CreateGradeConstantRequestDto.builder().value(gradeConstant.getValue())
                .name(gradeConstant.getName())
                .description(gradeConstant.getDescription())
                .build();

        // Мок сервис, чтобы возвращал объект с id
        when(service.createGradeConstant(createGradeConstantRequest)).thenReturn(
                GradeConstant.builder()
                        .id(1L)
                        .value(6)
                        .name("6 оценка")
                        .description("6")
                        .build()
        );

        mockMvc.perform(post("/api/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeConstant))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("6 оценка"))
                .andExpect(jsonPath("$.description").value("6"))
                .andExpect(jsonPath("$.value").value(6));
    }

    @Test
    public void testUpdateGrade() throws Exception {
        Long gradeId = 1L;

        UpdateGradeConstantDto updateDto = UpdateGradeConstantDto.builder()
                .name("Обновленная оценка")
                .value(5)
                .description("Обновлено")
                .build();

        GradeConstant updatedGrade = GradeConstant.builder()
                .id(gradeId)
                .name(updateDto.getName())
                .value(updateDto.getValue())
                .description(updateDto.getDescription())
                .build();

        // Заглушка для сервиса
        when(service.updateGradeConstant(gradeId, updateDto)).thenReturn(updatedGrade);

        mockMvc.perform(put("/api/grades/{id}", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gradeId))
                .andExpect(jsonPath("$.name").value("Обновленная оценка"))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.description").value("Обновлено"));

        // Проверяем, что сервис был вызван
        verify(service).updateGradeConstant(gradeId, updateDto);
    }


    @Test
    public void testDeleteGrade() throws Exception {
        Long gradeId = 1L;

        // Мокируем сервис, чтобы метод ничего не делал
        // Можно использовать doNothing(), но в Mockito 3+ достаточно просто вызвать метод, если он void
        // doNothing().when(service).deleteGradeConstant(gradeId);

        mockMvc.perform(delete("/api/grades/{id}", gradeId))
                .andExpect(status().isOk());

        // Проверяем, что сервис действительно вызвался
        verify(service).deleteGradeConstant(gradeId);
    }


}
