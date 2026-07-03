package com.rusobr.user.service;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.TeacherMapper;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.application.service.teacher.TeacherService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock private TeacherRepository teacherRepository;
    @Mock private TeacherMapper teacherMapper;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private TeacherService service;

    private static final Long USER_ID = 1L;
    private static final Long TEACHER_ID = 1L;

    @Nested
    @DisplayName("getWithUserById")
    class GetWithUserById {

        @Test
        @DisplayName("успешно возвращает TeacherResponse, если учитель найден")
        void success() {
            Teacher teacher = Teacher.builder().id(TEACHER_ID).build();
            TeacherResponse expected = new TeacherResponse(
                    UserResponse.builder().id(USER_ID).build(),
                    new TeacherDetails("t@mail.com", "+7999")
            );

            when(teacherRepository.findWithUserById(TEACHER_ID)).thenReturn(Optional.of(teacher));
            when(teacherMapper.toTeacherResponse(teacher)).thenReturn(expected);

            TeacherResponse result = service.getWithUserById(TEACHER_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
            verify(teacherRepository).findWithUserById(TEACHER_ID);
        }

        @Test
        @DisplayName("учитель не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(teacherRepository.findWithUserById(TEACHER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getWithUserById(TEACHER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Teacher by id: " + TEACHER_ID + " not found");

            verifyNoInteractions(teacherMapper);
        }
    }

    @Nested
    @DisplayName("getDetailsById")
    class GetDetailsById {

        @Test
        @DisplayName("успешно возвращает TeacherDetails по id")
        void success() {
            Teacher teacher = Teacher.builder().id(TEACHER_ID).build();
            TeacherDetails expected = new TeacherDetails("t@mail.com", "+7999");

            when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher));
            when(teacherMapper.toTeacherDetails(teacher)).thenReturn(expected);

            TeacherDetails result = service.getDetailsById(TEACHER_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
            verify(teacherRepository).findById(TEACHER_ID);
        }

        @Test
        @DisplayName("учитель не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getDetailsById(TEACHER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Teacher by id: " + TEACHER_ID + " not found");

            verifyNoInteractions(teacherMapper);
        }
    }

    @Nested
    @DisplayName("getBatch")
    class GetBatch {

        @Test
        @DisplayName("успешно возвращает список UserFeignResponse по ids")
        void success() {
            UserProjection projection = mock(UserProjection.class);
            UserFeignResponse feignResponse = new UserFeignResponse(TEACHER_ID, "Ivan", "Ivanov", "ivan", "kc-1");

            when(teacherRepository.findAllTeachersByIds(List.of(TEACHER_ID))).thenReturn(List.of(projection));
            when(userMapper.toUserFeignResponse(projection)).thenReturn(feignResponse);

            BatchUserResponse result = service.getBatch(List.of(TEACHER_ID));

            assertThat(result.found()).hasSize(1).containsExactly(feignResponse);
            assertThat(result.notFound()).isEmpty();
        }

        @Test
        @DisplayName("пустой список ids — возвращает пустой список")
        void emptyIds_returnsEmpty() {
            BatchUserResponse result = service.getBatch(List.of());

            assertThat(result.found()).isEmpty();
            assertThat(result.notFound()).isEmpty();
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("getSimpleById")
    class GetSimpleById {

        @Test
        @DisplayName("успешно возвращает UserFeignResponse через проекцию")
        void success() {
            UserProjection projection = mock(UserProjection.class);
            UserFeignResponse expected = new UserFeignResponse(TEACHER_ID, "Ivan", "Ivanov", "ivan", "kc-1");

            when(teacherRepository.getTeacherSimpleById(TEACHER_ID)).thenReturn(projection);
            when(userMapper.toUserFeignResponse(projection)).thenReturn(expected);

            UserFeignResponse result = service.getSimpleById(TEACHER_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("findByIdWithDeleted")
    class FindByIdWithDeleted {

        @Test
        @DisplayName("возвращает Optional с учителем, даже если он мягко удалён")
        void found() {
            Teacher teacher = Teacher.builder().id(TEACHER_ID).build();
            when(teacherRepository.findByIdWithDeleted(TEACHER_ID)).thenReturn(Optional.of(teacher));

            Optional<Teacher> result = service.findByIdWithDeleted(TEACHER_ID);

            assertThat(result).isPresent().contains(teacher);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если учитель не существует")
        void notFound_returnsEmptyOptional() {
            when(teacherRepository.findByIdWithDeleted(TEACHER_ID)).thenReturn(Optional.empty());

            Optional<Teacher> result = service.findByIdWithDeleted(TEACHER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно находит пользователя и сохраняет нового учителя")
        void success() {
            User user = User.builder().id(USER_ID).build();
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            Teacher teacherToSave = Teacher.builder().user(user).build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(teacherMapper.toEntity(user, details)).thenReturn(teacherToSave);

            service.create(USER_ID, details);

            verify(teacherRepository).save(teacherToSave);
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException и не сохраняет учителя")
        void userNotFound_throwsException() {
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User by id: " + USER_ID + " not found");

            verifyNoInteractions(teacherMapper, teacherRepository);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно обновляет phoneNumber и email учителя")
        void success_updatesBothFields() {
            Teacher teacher = Teacher.builder().id(TEACHER_ID).phoneNumber("+7000").email("old@mail.com").build();
            TeacherDetails details = new TeacherDetails("new@mail.com", "+7999");

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(teacherRepository.findById(USER_ID)).thenReturn(Optional.of(teacher));

            service.update(USER_ID, details);

            assertThat(teacher.getPhoneNumber()).isEqualTo("+7999");
            assertThat(teacher.getEmail()).isEqualTo("new@mail.com");
        }

        @Test
        @DisplayName("phoneNumber == null — поле не меняется")
        void nullPhone_doesNotChangeField() {
            Teacher teacher = Teacher.builder().id(TEACHER_ID).phoneNumber("+7000").email("old@mail.com").build();
            TeacherDetails details = new TeacherDetails("new@mail.com", null);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(teacherRepository.findById(USER_ID)).thenReturn(Optional.of(teacher));

            service.update(USER_ID, details);

            assertThat(teacher.getPhoneNumber()).isEqualTo("+7000");
            assertThat(teacher.getEmail()).isEqualTo("new@mail.com");
        }

        @Test
        @DisplayName("email == null — поле не меняется")
        void nullEmail_doesNotChangeField() {
            Teacher teacher = Teacher.builder().id(TEACHER_ID).phoneNumber("+7000").email("old@mail.com").build();
            TeacherDetails details = new TeacherDetails(null, "+7999");

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(teacherRepository.findById(USER_ID)).thenReturn(Optional.of(teacher));

            service.update(USER_ID, details);

            assertThat(teacher.getPhoneNumber()).isEqualTo("+7999");
            assertThat(teacher.getEmail()).isEqualTo("old@mail.com");
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException")
        void userNotFound_throwsException() {
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.update(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User by id: " + USER_ID + " not found");

            verify(teacherRepository, never()).findById(any());
        }

        @Test
        @DisplayName("пользователь есть, но учитель не найден — бросает NotFoundException")
        void teacherNotFound_throwsException() {
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(teacherRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Teacher by id: " + USER_ID + " not found");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно вызывает удаление из репозитория по id")
        void success() {
            when(teacherRepository.existsById(TEACHER_ID)).thenReturn(true);

            service.delete(TEACHER_ID);

            verify(teacherRepository).deleteById(TEACHER_ID);
        }
    }

    @Nested
    @DisplayName("handleUserDelete")
    class HandleUserDelete {

        @Test
        @DisplayName("если в событии есть роль TEACHER — запускает удаление")
        void roleMatches_callsDelete() {
            UserDeletedEvent event = new UserDeletedEvent(TEACHER_ID, Set.of(UserRole.TEACHER, UserRole.STUDENT));
            when(teacherRepository.existsById(TEACHER_ID)).thenReturn(true);

            service.handleUserDelete(event);

            verify(teacherRepository).deleteById(TEACHER_ID);
        }

        @Test
        @DisplayName("если в событии нет роли TEACHER — ничего не делает")
        void roleMismatches_doesNothing() {
            UserDeletedEvent event = new UserDeletedEvent(TEACHER_ID, Set.of(UserRole.STUDENT, UserRole.PARENT));

            service.handleUserDelete(event);

            verify(teacherRepository, never()).deleteById(any());
        }
    }
}
