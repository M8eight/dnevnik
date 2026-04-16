package com.rusobr.academic.service;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.SubjectMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.infrastructure.service.SubjectService;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
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
public class SubjectServiceTest {

    @Mock SubjectRepository subjectRepository;
    @Mock SubjectMapper subjectMapper;

    @InjectMocks SubjectService service;

    @Nested
    @DisplayName("getSubjects")
    class GetSubjects {

        @Test
        @DisplayName("возвращает страницу маппированных DTO")
        void returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Subject subject = Subject.builder().id(1L).name("Математика").build();
            SubjectResponseDto dto = new SubjectResponseDto(1L, "Математика");
            Page<Subject> page = new PageImpl<>(List.of(subject));

            when(subjectRepository.findAll(pageable)).thenReturn(page);
            when(subjectMapper.toSubjectResponseDto(subject)).thenReturn(dto);

            Page<SubjectResponseDto> result = service.getSubjects(pageable);

            assertThat(result.getContent()).containsExactly(dto);
        }

        @Test
        @DisplayName("пустая страница — возвращает пустой Page")
        void emptyPage_returnsEmpty() {
            Pageable pageable = PageRequest.of(0, 10);

            when(subjectRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<SubjectResponseDto> result = service.getSubjects(pageable);

            assertThat(result.getContent()).isEmpty();
            verifyNoInteractions(subjectMapper);
        }
    }

    @Nested
    @DisplayName("getSubject")
    class GetSubject {

        @Test
        @DisplayName("возвращает маппированный DTO")
        void success() {
            Subject subject = Subject.builder().id(1L).name("Физика").build();
            SubjectResponseDto dto = new SubjectResponseDto(1L, "Физика");

            when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
            when(subjectMapper.toSubjectResponseDto(subject)).thenReturn(dto);

            SubjectResponseDto result = service.getSubject(1L);

            assertThat(result).isEqualTo(dto);
        }

        @Test
        @DisplayName("предмет не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSubject(99L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Subject not found 99");

            verifyNoInteractions(subjectMapper);
        }
    }

    @Nested
    @DisplayName("createSubject")
    class CreateSubject {

        @Test
        @DisplayName("успешно создаёт предмет")
        void success() {
            SubjectRequestDto req = new SubjectRequestDto("Химия");
            Subject subject = Subject.builder().name("Химия").build();
            Subject saved = Subject.builder().id(1L).name("Химия").build();
            SubjectResponseDto dto = new SubjectResponseDto(1L, "Химия");

            when(subjectMapper.toSubject(req)).thenReturn(subject);
            when(subjectRepository.save(subject)).thenReturn(saved);
            when(subjectMapper.toSubjectResponseDto(saved)).thenReturn(dto);

            SubjectResponseDto result = service.createSubject(req);

            assertThat(result).isEqualTo(dto);
            verify(subjectRepository).save(subject);
        }
    }

    @Nested
    @DisplayName("updateSubject")
    class UpdateSubject {

        @Test
        @DisplayName("успешно обновляет предмет")
        void success() {
            SubjectRequestDto req = new SubjectRequestDto("Биология");
            Subject subject = Subject.builder().id(1L).name("Химия").build();
            SubjectResponseDto dto = new SubjectResponseDto(1L, "Биология");

            when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
            doAnswer(inv -> { subject.setName("Биология"); return null; })
                    .when(subjectMapper).updateEntityFromDto(req, subject);
            when(subjectMapper.toSubjectResponseDto(subject)).thenReturn(dto);

            SubjectResponseDto result = service.updateSubject(1L, req);

            assertThat(result).isEqualTo(dto);
            verify(subjectMapper).updateEntityFromDto(req, subject);
        }

        @Test
        @DisplayName("предмет не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            SubjectRequestDto req = new SubjectRequestDto("Биология");

            when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateSubject(99L, req))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Subject not found 99");

            verifyNoInteractions(subjectMapper);
        }
    }

    @Nested
    @DisplayName("deleteSubject")
    class DeleteSubject {

        @Test
        @DisplayName("успешно удаляет предмет")
        void success() {
            when(subjectRepository.existsById(1L)).thenReturn(true);

            service.deleteSubject(1L);

            verify(subjectRepository).deleteById(1L);
        }

        @Test
        @DisplayName("предмет не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(subjectRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteSubject(99L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Subject not found 99");

            verify(subjectRepository, never()).deleteById(any());
        }
    }
}