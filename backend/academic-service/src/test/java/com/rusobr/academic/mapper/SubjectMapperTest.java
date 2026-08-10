package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.SubjectMapper;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.infrastructure.persistence.projection.SubjectResponseProjection;
import com.rusobr.academic.web.dto.subject.SubjectRequest;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubjectMapperTest {

    private final SubjectMapper mapper = Mappers.getMapper(SubjectMapper.class);

    @Test
    void shouldMapAllFieldsCorrectly() {
        Subject subject = Subject.builder()
                .id(1L)
                .name("Алгебра")
                .build();

        SubjectResponseDto result = mapper.toSubjectResponseDto(subject);

        assertThat(result).isEqualTo(new SubjectResponseDto(1L, "Алгебра"));
    }

    @Test
    void shouldReturnNullWhenSourceIsNull() {
        assertThat(mapper.toSubjectResponseDto((Subject) null)).isNull();
    }

    @Test
    void shouldMapSubjectNameToNameWhenCreatingEntity() {
        SubjectRequest request = new SubjectRequest("Алгебра");

        Subject result = mapper.toSubject(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Алгебра");
    }

    @Test
    void shouldReturnNullEntityWhenRequestIsNull() {
        assertThat(mapper.toSubject(null)).isNull();
    }

    @Test
    void shouldMapNullSubjectNameAsNullName() {
        SubjectRequest request = new SubjectRequest(null);

        Subject result = mapper.toSubject(request);

        assertThat(result.getName()).isNull();
    }

    @Test
    void shouldUpdateNameAndKeepIdWhenUpdatingEntity() {
        Subject subject = Subject.builder()
                .id(1L)
                .name("Старое имя")
                .build();
        SubjectRequest request = new SubjectRequest("Алгебра");

        mapper.updateEntityFromDto(request, subject);

        assertThat(subject.getId()).isEqualTo(1L);
        assertThat(subject.getName()).isEqualTo("Алгебра");
    }

    @Test
    void shouldDoNothingWhenUpdatingWithNullDto() {
        Subject subject = Subject.builder()
                .id(1L)
                .name("Старое имя")
                .build();

        mapper.updateEntityFromDto(null, subject);

        assertThat(subject.getId()).isEqualTo(1L);
        assertThat(subject.getName()).isEqualTo("Старое имя");
    }

    @Test
    void shouldOverwriteNameWhenDtoNameIsNull() {
        Subject subject = Subject.builder()
                .id(1L)
                .name("Старое имя")
                .build();
        SubjectRequest request = new SubjectRequest(null);

        mapper.updateEntityFromDto(request, subject);

        assertThat(subject.getName()).isNull();
    }

    @Test
    void shouldMapAllFieldsFromProjection() {
        SubjectResponseProjection projection = mock(SubjectResponseProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getName()).thenReturn("Алгебра");

        SubjectResponseDto result = mapper.toSubjectResponseDto(projection);

        assertThat(result).isEqualTo(new SubjectResponseDto(1L, "Алгебра"));
    }

    @Test
    void shouldReturnNullWhenProjectionIsNull() {
        assertThat(mapper.toSubjectResponseDto((SubjectResponseProjection) null)).isNull();
    }
}
