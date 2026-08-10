package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.TeacherSubjectMapper;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeacherSubjectId;
import com.rusobr.academic.web.dto.feign.teacherInfo.TeacherSubjectRawResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectResponse;
import com.rusobr.common.dto.UserFeignResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class TeacherSubjectMapperTest {

    private final TeacherSubjectMapper mapper = Mappers.getMapper(TeacherSubjectMapper.class);

    @Test
    void shouldMapAllFieldsToResponse() {
        Subject subject = Subject.builder().id(7L).name("Алгебра").build();
        TeacherSubject teacherSubject = TeacherSubject.builder()
                .id(TeacherSubjectId.builder().teacherId(10L).subjectId(7L).build())
                .subject(subject)
                .build();
        UserFeignResponse teacher = UserFeignResponse.builder()
                .id(10L)
                .firstName("Иван")
                .lastName("Петров")
                .username("ivan.petrov")
                .keycloakId("kc-1")
                .build();

        TeacherSubjectResponse result = mapper.toResponse(teacherSubject, teacher);

        assertThat(result).isEqualTo(new TeacherSubjectResponse(
                teacher,
                new SubjectResponseDto(7L, "Алгебра")));
    }

    @Test
    void shouldReturnNullWhenBothSourcesAreNull() {
        assertThat(mapper.toResponse(null, null)).isNull();
    }

    @Test
    void shouldReturnNullTeacherWhenTeacherIsNull() {
        Subject subject = Subject.builder().id(7L).name("Алгебра").build();
        TeacherSubject teacherSubject = TeacherSubject.builder()
                .id(TeacherSubjectId.builder().teacherId(10L).subjectId(7L).build())
                .subject(subject)
                .build();

        TeacherSubjectResponse result = mapper.toResponse(teacherSubject, null);

        assertThat(result.teacher()).isNull();
        assertThat(result.subject()).isEqualTo(new SubjectResponseDto(7L, "Алгебра"));
    }

    @Test
    void shouldMapAllFieldsToRawResponse() {
        Subject subject = Subject.builder().id(7L).name("Алгебра").build();
        TeacherSubject teacherSubject = TeacherSubject.builder()
                .id(TeacherSubjectId.builder().teacherId(10L).subjectId(7L).build())
                .subject(subject)
                .build();

        TeacherSubjectRawResponse result = mapper.toRawResponse(teacherSubject);

        assertThat(result).isEqualTo(new TeacherSubjectRawResponse(new SubjectResponseDto(7L, "Алгебра")));
    }

    @Test
    void shouldReturnNullWhenRawSourceIsNull() {
        assertThat(mapper.toRawResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullSubjectWhenNestedIsNull() {
        TeacherSubject teacherSubject = TeacherSubject.builder()
                .id(TeacherSubjectId.builder().teacherId(10L).subjectId(7L).build())
                .build();

        TeacherSubjectRawResponse result = mapper.toRawResponse(teacherSubject);

        assertThat(result.subject()).isNull();
    }
}
