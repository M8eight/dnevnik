package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.SubjectMapper;
import com.rusobr.academic.application.service.SubjectService;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.infrastructure.persistence.projection.SubjectResponseProjection;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.web.dto.subject.SubjectRequest;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock private SubjectRepository subjectRepository;
    @Mock private SubjectMapper subjectMapper;

    @InjectMocks private SubjectService service;

    private static final Long SUBJECT_ID = 1L;
    private static final String SUBJECT_NAME = "Математика";

    @Nested
    @DisplayName("getById")
    class GetById {
        @Test
        @DisplayName("успешно возвращает предмет по ID")
        void success() {
            Subject subject = new Subject();
            SubjectResponseDto responseDto = new SubjectResponseDto(SUBJECT_ID, SUBJECT_NAME);

            when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(subject));
            when(subjectMapper.toSubjectResponseDto(subject)).thenReturn(responseDto);

            SubjectResponseDto result = service.getById(SUBJECT_ID);

            assertThat(result).isEqualTo(responseDto);
        }

        @Test
        @DisplayName("бросает NotFoundException, если предмет не найден")
        void notFound() {
            when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(SUBJECT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Subject with id: " + SUBJECT_ID + " not found");
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {
        @Test
        @DisplayName("успешно возвращает страницу с предметами (через проекции)")
        void success() {
            Pageable pageable = PageRequest.of(0, 10);

            // Мокаем интерфейс проекции
            SubjectResponseProjection projection = mock(SubjectResponseProjection.class);
            Page<SubjectResponseProjection> page = new PageImpl<>(List.of(projection));

            SubjectResponseDto responseDto = new SubjectResponseDto(SUBJECT_ID, SUBJECT_NAME);

            when(subjectRepository.findAllByOrderByNameAsc(pageable)).thenReturn(page);
            when(subjectMapper.toSubjectResponseDto(projection)).thenReturn(responseDto);

            Page<SubjectResponseDto> result = service.getAll(pageable);

            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(responseDto);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("успешно создает и сохраняет предмет")
        void success() {
            SubjectRequest requestDto = mock(SubjectRequest.class);
            Subject subject = new Subject();
            Subject savedSubject = new Subject();
            SubjectResponseDto responseDto = new SubjectResponseDto(SUBJECT_ID, SUBJECT_NAME);

            when(subjectMapper.toSubject(requestDto)).thenReturn(subject);
            when(subjectRepository.save(subject)).thenReturn(savedSubject);
            when(subjectMapper.toSubjectResponseDto(savedSubject)).thenReturn(responseDto);

            SubjectResponseDto result = service.create(requestDto);

            assertThat(result).isEqualTo(responseDto);
            verify(subjectRepository).save(subject);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("успешно удаляет предмет по ID")
        void success() {
            when(subjectRepository.existsById(SUBJECT_ID)).thenReturn(true);

            service.delete(SUBJECT_ID);

            verify(subjectRepository).deleteById(SUBJECT_ID);
        }

        @Test
        @DisplayName("бросает NotFoundException, если предмет для удаления не существует")
        void notFound() {
            when(subjectRepository.existsById(SUBJECT_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(SUBJECT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Subject with id: " + SUBJECT_ID + " not found");

            verify(subjectRepository, never()).deleteById(any());
        }
    }
}