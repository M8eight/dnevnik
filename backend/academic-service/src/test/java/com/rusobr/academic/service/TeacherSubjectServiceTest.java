package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.TeacherSubjectMapper;
import com.rusobr.academic.application.service.TeacherSubjectService;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeacherSubjectId;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeacherSubjectRepository;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectRequest;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherSubjectServiceTest {

    @Mock private TeacherSubjectRepository teacherSubjectRepository;
    @Mock private TeacherSubjectMapper teacherSubjectMapper;
    @Mock private UserClient userClient;
    @Mock private SubjectRepository subjectRepository;

    @InjectMocks private TeacherSubjectService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", service);
    }

    private static final Long TEACHER_ID = 5L;
    private static final Long SUBJECT_ID = 10L;
    private static final TeacherSubjectId TS_ID = new TeacherSubjectId(TEACHER_ID, SUBJECT_ID);

    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        @DisplayName("успешно возвращает список с прикрепленными данными учителей")
        void success() {
            TeacherSubject teacherSubject = TeacherSubject.builder().id(TS_ID).build();
            UserFeignResponse teacherResponse = new UserFeignResponse(TEACHER_ID, "Иван", "Иванов", "ivan", "key");
            TeacherSubjectResponse expectedResponse = new TeacherSubjectResponse(teacherResponse, new SubjectResponseDto(SUBJECT_ID, "Физика"));

            when(teacherSubjectRepository.findAll()).thenReturn(List.of(teacherSubject));
            when(userClient.getBatchTeachers(List.of(TEACHER_ID))).thenReturn(new BatchUserResponse(List.of(teacherResponse), List.of()));
            when(teacherSubjectMapper.toResponse(teacherSubject, teacherResponse)).thenReturn(expectedResponse);

            List<TeacherSubjectResponse> result = service.findAll();

            assertThat(result).hasSize(1).containsExactly(expectedResponse);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        private final TeacherSubjectRequest request = new TeacherSubjectRequest(TEACHER_ID, SUBJECT_ID);
        private final UserFeignResponse teacherResponse = new UserFeignResponse(TEACHER_ID, "Иван", "Иванов", "ivan", "key");

        @Test
        @DisplayName("бросает ConflictException, если связь уже существует и не удалена")
        void conflict_whenAlreadyExistsAndActive() {
            TeacherSubject existingActive = mock(TeacherSubject.class);

            when(userClient.getTeacherSimpleById(TEACHER_ID)).thenReturn(teacherResponse);
            when(teacherSubjectRepository.findByIdWithDeleted(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(existingActive));
            when(existingActive.getDeletedAt()).thenReturn(null); // Запись активна (не удалена)

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exist");

            verify(teacherSubjectRepository, never()).save(any());
        }

        @Test
        @DisplayName("восстанавливает запись, если она была мягко удалена")
        void success_restoreDeleted() {
            TeacherSubject softDeleted = mock(TeacherSubject.class);
            TeacherSubjectResponse expectedResponse = mock(TeacherSubjectResponse.class);

            when(userClient.getTeacherSimpleById(TEACHER_ID)).thenReturn(teacherResponse);
            when(teacherSubjectRepository.findByIdWithDeleted(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(softDeleted));
            when(softDeleted.getDeletedAt()).thenReturn(Instant.now());
            when(teacherSubjectRepository.save(softDeleted)).thenReturn(softDeleted);
            when(teacherSubjectMapper.toResponse(softDeleted, teacherResponse)).thenReturn(expectedResponse);

            TeacherSubjectResponse result = service.create(request);

            assertThat(result).isEqualTo(expectedResponse);
            verify(softDeleted).setDeletedAt(null); // Проверяем, что флаг удаления снят
            verify(teacherSubjectRepository).save(softDeleted);
        }

        @Test
        @DisplayName("создает новую связь, если ее раньше не было")
        void success_createNew() {
            Subject subject = new Subject();
            TeacherSubjectResponse expectedResponse = mock(TeacherSubjectResponse.class);

            when(userClient.getTeacherSimpleById(TEACHER_ID)).thenReturn(teacherResponse);
            when(teacherSubjectRepository.findByIdWithDeleted(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.empty());
            when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(subject));

            // Настраиваем мок так, чтобы при сохранении любого TeacherSubject возвращался он же
            when(teacherSubjectRepository.save(any(TeacherSubject.class))).thenAnswer(i -> i.getArgument(0));
            when(teacherSubjectMapper.toResponse(any(TeacherSubject.class), eq(teacherResponse))).thenReturn(expectedResponse);

            TeacherSubjectResponse result = service.create(request);

            assertThat(result).isEqualTo(expectedResponse);
            verify(teacherSubjectRepository).save(argThat(ts ->
                    ts.getId().equals(TS_ID) && ts.getSubject().equals(subject)
            ));
        }

        @Test
        @DisplayName("бросает NotFoundException при создании, если предмет не существует")
        void notFound_whenSubjectDoesNotExist() {
            when(userClient.getTeacherSimpleById(TEACHER_ID)).thenReturn(teacherResponse);
            when(teacherSubjectRepository.findByIdWithDeleted(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.empty());
            when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Subject with id: " + SUBJECT_ID + " not found");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        private final TeacherSubjectRequest request = new TeacherSubjectRequest(TEACHER_ID, SUBJECT_ID);

        @Test
        @DisplayName("успешно выполняет мягкое удаление, если связь существует")
        void success() {
            when(teacherSubjectRepository.existsById(TS_ID)).thenReturn(true);

            service.delete(request);

            verify(teacherSubjectRepository).softDelete(SUBJECT_ID, TEACHER_ID);
        }

        @Test
        @DisplayName("бросает ConflictException, если связь для удаления не найдена")
        void conflict_whenRelationDoesNotExist() {
            when(teacherSubjectRepository.existsById(TS_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("not found");

            verify(teacherSubjectRepository, never()).softDelete(any(), any());
        }
    }
}
