package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.AcademicYearMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AcademicYearMapperTest {

    private final AcademicYearMapper mapper = Mappers.getMapper(AcademicYearMapper.class);

    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 5, 31);

    @Test
    void shouldMapAllFieldsToResponse() {
        AcademicYear year = AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .description("Учебный год")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .closed(true)
                .build();

        AcademicYearResponse result = mapper.toResponse(year);

        assertThat(result).isEqualTo(new AcademicYearResponse(1L, "2025-2026", "Учебный год", START_DATE, END_DATE, true));
    }

    @Test
    void shouldReturnNullResponseWhenSourceIsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void shouldMapNullDescriptionAsNull() {
        AcademicYear year = AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();

        AcademicYearResponse result = mapper.toResponse(year);

        assertThat(result.description()).isNull();
    }

    @Test
    void shouldMapOpenYearToFalse() {
        AcademicYear year = AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();

        AcademicYearResponse result = mapper.toResponse(year);

        assertThat(result.closed()).isFalse();
    }

    @Test
    void shouldMapAllFieldsToEntity() {
        AcademicYearRequest request = new AcademicYearRequest("2025-2026", "Учебный год", START_DATE, END_DATE);

        AcademicYear result = mapper.toEntity(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("2025-2026");
        assertThat(result.getDescription()).isEqualTo("Учебный год");
        assertThat(result.getStartDate()).isEqualTo(START_DATE);
        assertThat(result.getEndDate()).isEqualTo(END_DATE);
    }

    @Test
    void shouldReturnNullEntityWhenRequestIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void shouldMapNullDescriptionInRequestAsNull() {
        AcademicYearRequest request = new AcademicYearRequest("2025-2026", null, START_DATE, END_DATE);

        AcademicYear result = mapper.toEntity(request);

        assertThat(result.getDescription()).isNull();
    }
}
