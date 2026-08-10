package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AcademicPeriodMapperTest {

    private final AcademicPeriodMapper mapper = Mappers.getMapper(AcademicPeriodMapper.class);

    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2025, 12, 31);

    @Test
    void shouldMapAllFieldsToResponse() {
        AcademicYear year = AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .closed(true)
                .build();
        AcademicPeriod period = AcademicPeriod.builder()
                .id(1L)
                .name("I полугодие")
                .academicYear(year)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .closed(true)
                .build();

        AcademicPeriodResponse result = mapper.toResponse(period);

        assertThat(result).isEqualTo(new AcademicPeriodResponse(
                1L,
                "I полугодие",
                new AcademicYearResponse(1L, "2025-2026", null, START_DATE, END_DATE, true),
                true,
                START_DATE,
                END_DATE));
    }

    @Test
    void shouldReturnNullResponseWhenSourceIsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void shouldMapClosedToIsClosed() {
        AcademicPeriod openPeriod = AcademicPeriod.builder()
                .id(1L)
                .name("I полугодие")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
        AcademicPeriod closedPeriod = AcademicPeriod.builder()
                .id(2L)
                .name("II полугодие")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .closed(true)
                .build();

        assertThat(mapper.toResponse(openPeriod).isClosed()).isFalse();
        assertThat(mapper.toResponse(closedPeriod).isClosed()).isTrue();
    }

    @Test
    void shouldReturnNullAcademicYearWhenNestedIsNull() {
        AcademicPeriod period = AcademicPeriod.builder()
                .id(1L)
                .name("I полугодие")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();

        AcademicPeriodResponse result = mapper.toResponse(period);

        assertThat(result.academicYear()).isNull();
    }

    @Test
    void shouldMapNameAndDatesToEntity() {
        AcademicPeriodRequest request = new AcademicPeriodRequest("I полугодие", 7L, START_DATE, END_DATE);

        AcademicPeriod result = mapper.toEntity(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("I полугодие");
        assertThat(result.getStartDate()).isEqualTo(START_DATE);
        assertThat(result.getEndDate()).isEqualTo(END_DATE);
    }

    @Test
    void shouldReturnNullEntityWhenRequestIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void shouldNotMapAcademicYearIdToNestedEntity() {
        AcademicPeriodRequest request = new AcademicPeriodRequest("I полугодие", 7L, START_DATE, END_DATE);

        AcademicPeriod result = mapper.toEntity(request);

        assertThat(result.getAcademicYear()).isNull();
    }
}
