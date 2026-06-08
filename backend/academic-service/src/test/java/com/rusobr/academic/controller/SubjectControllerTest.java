package com.rusobr.academic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.academic.application.service.SubjectService;
import com.rusobr.academic.web.controller.SubjectController;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubjectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private SubjectService subjectService;

    private static final Long SUBJECT_ID = 11L;
    private static final String SUBJECT_NAME = "Mathematics";

    private SubjectResponseDto buildSubjectResponse() {
        return new SubjectResponseDto(SUBJECT_ID, SUBJECT_NAME);
    }

    @Test
    @DisplayName("GET /subjects — 200 and page")
    void getAll_ShouldReturn200() throws Exception {
        Page<SubjectResponseDto> page = new PageImpl<>(List.of(buildSubjectResponse()), PageRequest.of(0, 20), 1);
        when(subjectService.getAll(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/subjects").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(SUBJECT_ID))
                .andExpect(jsonPath("$.content[0].name").value(SUBJECT_NAME));
    }

    @Test
    @DisplayName("POST /subjects — 200 and create")
    void create_ShouldReturn200() throws Exception {
        SubjectRequestDto request = new SubjectRequestDto(SUBJECT_NAME);
        when(subjectService.create(request)).thenReturn(buildSubjectResponse());

        mockMvc.perform(post("/api/v1/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SUBJECT_ID))
                .andExpect(jsonPath("$.name").value(SUBJECT_NAME));
    }

    @Test
    @DisplayName("DELETE /subjects/{id} — 200 on delete")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(subjectService).delete(SUBJECT_ID);

        mockMvc.perform(delete("/api/v1/subjects/{id}", SUBJECT_ID))
                .andExpect(status().isOk());

        verify(subjectService).delete(SUBJECT_ID);
    }
}
