package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.SchoolClassMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchoolClassMapperTest {

    private final SchoolClassMapper mapper = Mappers.getMapper(SchoolClassMapper.class);

    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 5, 31);

    @Test
    void shouldMapAllFieldsToSchoolClassResponse() {
        AcademicYear year = AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
        SchoolClass schoolClass = SchoolClass.builder()
                .id(5L)
                .name("5А")
                .academicYear(year)
                .classTeacherId(10L)
                .build();

        SchoolClassResponse result = mapper.toSchoolClassResponse(schoolClass);

        assertThat(result).isEqualTo(new SchoolClassResponse(
                5L,
                "5А",
                new AcademicYearResponse(1L, "2025-2026", null, START_DATE, END_DATE, false),
                10L));
    }

    @Test
    void shouldReturnNullWhenResponseSourceIsNull() {
        assertThat(mapper.toSchoolClassResponse(null)).isNull();
    }

    @Test
    void shouldMapRequestToEntityWithOnlyAcademicYearId() {
        AcademicYear year = AcademicYear.builder().id(1L).name("2025-2026").build();
        SchoolClassRequest request = new SchoolClassRequest("5А", 1L);

        SchoolClass result = mapper.toSchoolClass(request, year);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("5А");
        assertThat(result.getClassTeacherId()).isNull();
        assertThat(result.getAcademicYear().getId()).isEqualTo(1L);
        assertThat(result.getAcademicYear().getName()).isNull();
    }

    @Test
    void shouldReturnNullWhenRequestAndYearAreNull() {
        assertThat(mapper.toSchoolClass(null, null)).isNull();
    }

    @Test
    void shouldMapNullAcademicYearAsNull() {
        SchoolClassRequest request = new SchoolClassRequest("5А", 1L);

        SchoolClass result = mapper.toSchoolClass(request, null);

        assertThat(result.getName()).isEqualTo("5А");
        assertThat(result.getAcademicYear()).isNull();
    }

    @Test
    void shouldMapAllParametersToFullResponse() {
        SchoolClass schoolClass = SchoolClass.builder()
                .id(5L)
                .name("5А")
                .classTeacherId(10L)
                .build();
        BatchUserResponse users = BatchUserResponse.ok(
                List.of(UserFeignResponse.builder().id(1L).firstName("Иван").build()),
                List.of());
        TeacherResponse teacher = new TeacherResponse(
                UserFeignResponse.builder().id(10L).firstName("Иван").lastName("Петров").build(),
                null);

        SchoolClassFullResponse result = mapper.toSchoolClassFullResponse(schoolClass, users, teacher, 10L);

        assertThat(result).isEqualTo(new SchoolClassFullResponse(5L, "5А", teacher, 10L, users));
    }

    @Test
    void shouldReturnNullWhenAllSourcesAreNull() {
        assertThat(mapper.toSchoolClassFullResponse(null, null, null, null)).isNull();
    }

    @Test
    void shouldReturnNullStudentsWhenUsersIsNull() {
        SchoolClass schoolClass = SchoolClass.builder()
                .id(5L)
                .name("5А")
                .build();

        SchoolClassFullResponse result = mapper.toSchoolClassFullResponse(schoolClass, null, null, null);

        assertThat(result.students()).isNull();
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("5А");
    }
}
