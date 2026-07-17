package com.rusobr.user.service;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.StudentMapper;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.application.service.student.StudentService;
import com.rusobr.user.application.service.teacher.TeacherService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.feign.AcademicClient;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import com.rusobr.user.web.dto.feign.AcademicYearResponse;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.ConflictException;
import com.rusobr.user.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private StudentMapper studentMapper;
    @Mock private AcademicClient academicClient;
    @Mock private TeacherService teacherService;
    @Mock private ParentRepository parentRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private StudentService service;

    private static final Long USER_ID = 1L;
    private static final Long STUDENT_ID = 1L;
    private static final Long PARENT_ID = 2L;

    private AcademicYearResponse buildAcademicYearResponse() {
        return new AcademicYearResponse(
                2L,
                "2024-2025",
                "Учебный год 2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 5, 31),
                true
        );
    }

    @Nested
    @DisplayName("getBatch")
    class GetBatch {

        @Test
        @DisplayName("возвращает пустой список, если ids == null")
        void nullIds_returnsEmpty() {
            BatchUserResponse result = service.getBatch(null);
            assertThat(result.found()).isEmpty();
            assertThat(result.notFound()).isEmpty();
            verifyNoInteractions(studentRepository, userMapper);
        }

        @Test
        @DisplayName("возвращает пустой список, если ids пустой")
        void emptyIds_returnsEmpty() {
            BatchUserResponse result = service.getBatch(Collections.emptyList());
            assertThat(result.found()).isEmpty();
            assertThat(result.notFound()).isEmpty();
            verifyNoInteractions(studentRepository, userMapper);
        }

        @Test
        @DisplayName("успешно возвращает список UserFeignResponse по ids")
        void success() {
            UserProjection projection = mock(UserProjection.class);
            UserFeignResponse feignResponse = new UserFeignResponse(STUDENT_ID, "Ivan", "Ivanov", "ivan", "kc-1");

            when(studentRepository.findAllStudentsByIds(List.of(STUDENT_ID))).thenReturn(List.of(projection));
            when(userMapper.toUserFeignResponse(projection)).thenReturn(feignResponse);

            BatchUserResponse result = service.getBatch(List.of(STUDENT_ID));

            assertThat(result.found()).hasSize(1).containsExactly(feignResponse);
            assertThat(result.notFound()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBatchWithExcludingIds")
    class GetBatchWithExcludingIds {

        @Test
        @DisplayName("если ids пустой — возвращает всех студентов")
        void emptyIds_returnsAll() {
            UserProjection projection = mock(UserProjection.class);
            UserFeignResponse feignResponse = new UserFeignResponse(STUDENT_ID, "Ivan", "Ivanov", "ivan", "kc-1");

            when(studentRepository.findWithUserAllStudents()).thenReturn(List.of(projection));
            when(userMapper.toUserFeignResponse(projection)).thenReturn(feignResponse);

            List<UserFeignResponse> result = service.getBatchWithExcludingIds(Collections.emptySet());

            assertThat(result).hasSize(1).containsExactly(feignResponse);
            verify(studentRepository).findWithUserAllStudents();
            verify(studentRepository, never()).findAllStudentsExcludeAssigned(any());
        }

        @Test
        @DisplayName("если ids непустой — возвращает студентов без указанных")
        void nonEmptyIds_returnsExcluded() {
            UserProjection projection = mock(UserProjection.class);
            UserFeignResponse feignResponse = new UserFeignResponse(2L, "Petr", "Petrov", "petr", "kc-2");

            when(studentRepository.findAllStudentsExcludeAssigned(Set.of(STUDENT_ID))).thenReturn(List.of(projection));
            when(userMapper.toUserFeignResponse(projection)).thenReturn(feignResponse);

            List<UserFeignResponse> result = service.getBatchWithExcludingIds(Set.of(STUDENT_ID));

            assertThat(result).hasSize(1).containsExactly(feignResponse);
            verify(studentRepository).findAllStudentsExcludeAssigned(Set.of(STUDENT_ID));
            verify(studentRepository, never()).findWithUserAllStudents();
        }
    }

    @Nested
    @DisplayName("getDetailsById")
    class GetDetailsById {

        @Test
        @DisplayName("успешно возвращает StudentDetails по id")
        void success() {
            Student student = Student.builder().id(STUDENT_ID).build();
            StudentDetails expectedDetails = new StudentDetails("math");

            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(studentMapper.toStudentDetails(student)).thenReturn(expectedDetails);

            StudentDetails result = service.getDetailsById(STUDENT_ID);

            assertThat(result).isNotNull().isEqualTo(expectedDetails);
            verify(studentRepository).findById(STUDENT_ID);
        }

        @Test
        @DisplayName("студент не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getDetailsById(STUDENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " not found");

            verifyNoInteractions(studentMapper);
        }
    }

    @Nested
    @DisplayName("getWithClassById")
    class GetWithClassById {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(service, "self", service);
        }

        @Test
        @DisplayName("успешно возвращает StudentWithClassResponse с данными класса и учителя")
        void success() {
            User user = User.builder().id(USER_ID).build();
            Student student = Student.builder().id(STUDENT_ID).user(user).build();
            SchoolClassResponse schoolClass = new SchoolClassResponse(10L, "10А", buildAcademicYearResponse(), 5L);
            TeacherResponse teacher = new TeacherResponse(
                    UserResponse.builder().id(5L).build(),
                    new TeacherDetails("t@mail.com", "+7999")
            );
            StudentWithClassResponse expected = new StudentWithClassResponse(
                    STUDENT_ID, "Ivan", "Ivanov", "math", schoolClass, teacher
            );

            when(studentRepository.findWithUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(academicClient.getSchoolClassByStudentId(STUDENT_ID)).thenReturn(schoolClass);
            when(teacherService.getWithUserById(5L)).thenReturn(teacher);
            when(studentMapper.toStudentDetailResponse(student, schoolClass, teacher)).thenReturn(expected);

            StudentWithClassResponse result = service.getWithClassById(STUDENT_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
            verify(academicClient).getSchoolClassByStudentId(STUDENT_ID);
            verify(teacherService).getWithUserById(5L);
        }

        @Test
        @DisplayName("студент не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(studentRepository.findWithUserById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getWithClassById(STUDENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " not found");

            verifyNoInteractions(academicClient, teacherService, studentMapper);
        }
    }

    @Nested
    @DisplayName("findByIdWithDeleted")
    class FindByIdWithDeleted {

        @Test
        @DisplayName("возвращает Optional с студентом, даже если он мягко удалён")
        void found() {
            Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findByIdWithDeleted(STUDENT_ID)).thenReturn(Optional.of(student));

            Optional<Student> result = service.findByIdWithDeleted(STUDENT_ID);

            assertThat(result).isPresent().contains(student);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если студент не существует")
        void notFound_returnsEmptyOptional() {
            when(studentRepository.findByIdWithDeleted(STUDENT_ID)).thenReturn(Optional.empty());

            Optional<Student> result = service.findByIdWithDeleted(STUDENT_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно находит пользователя и сохраняет нового студента")
        void success() {
            User user = User.builder().id(USER_ID).build();
            StudentDetails details = new StudentDetails("physics");
            Student studentToSave = Student.builder().user(user).build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(studentMapper.toEntity(user, details)).thenReturn(studentToSave);

            service.create(USER_ID, details);

            verify(studentRepository).save(studentToSave);
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException и не сохраняет студента")
        void userNotFound_throwsException() {
            StudentDetails details = new StudentDetails("physics");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User by id: " + USER_ID + " not found");

            verifyNoInteractions(studentMapper, studentRepository);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно обновляет studyProfile студента")
        void success() {
            Student student = Student.builder().id(STUDENT_ID).studyProfile("math").build();
            StudentDetails details = new StudentDetails("physics");

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(studentRepository.findById(USER_ID)).thenReturn(Optional.of(student));

            service.update(USER_ID, details);

            assertThat(student.getStudyProfile()).isEqualTo("physics");
        }

        @Test
        @DisplayName("studyProfile == null — поле не меняется")
        void nullProfile_doesNotChangeField() {
            Student student = Student.builder().id(STUDENT_ID).studyProfile("math").build();
            StudentDetails details = new StudentDetails(null);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(studentRepository.findById(USER_ID)).thenReturn(Optional.of(student));

            service.update(USER_ID, details);

            assertThat(student.getStudyProfile()).isEqualTo("math");
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException")
        void userNotFound_throwsException() {
            StudentDetails details = new StudentDetails("math");
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.update(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User by id: " + USER_ID + " not found");

            verify(studentRepository, never()).findById(any());
        }

        @Test
        @DisplayName("пользователь есть, но студент не найден — бросает NotFoundException")
        void studentNotFound_throwsException() {
            StudentDetails details = new StudentDetails("math");
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(studentRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Student by id: " + USER_ID + " not found");
        }
    }

    @Nested
    @DisplayName("assignToParent")
    class AssignToParent {

        @Test
        @DisplayName("успешно привязывает студента к родителю")
        void success() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            Student student = Student.builder().id(STUDENT_ID).parent(null).build();

            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.of(parent));
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

            service.assignToParent(STUDENT_ID, PARENT_ID);

            assertThat(student.getParent()).isEqualTo(parent);
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("студент уже привязан к этому же родителю — бросает ConflictException")
        void alreadySameParent_throwsConflict() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            Student student = Student.builder().id(STUDENT_ID).parent(parent).build();

            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.of(parent));
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> service.assignToParent(STUDENT_ID, PARENT_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " already has parent");

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("студент уже привязан к другому родителю — бросает ConflictException")
        void alreadyOtherParent_throwsConflict() {
            Parent existingParent = Parent.builder().id(99L).build();
            Parent newParent = Parent.builder().id(PARENT_ID).build();
            Student student = Student.builder().id(STUDENT_ID).parent(existingParent).build();

            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.of(newParent));
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> service.assignToParent(STUDENT_ID, PARENT_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " already has parent");

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("родитель не найден — бросает NotFoundException")
        void parentNotFound_throwsException() {
            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assignToParent(STUDENT_ID, PARENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Parent by id: " + PARENT_ID + " not found");

            verifyNoInteractions(studentRepository);
        }

        @Test
        @DisplayName("студент не найден — бросает NotFoundException")
        void studentNotFound_throwsException() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.of(parent));
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assignToParent(STUDENT_ID, PARENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " not found");
        }
    }

    @Nested
    @DisplayName("unassignFromParent")
    class UnassignFromParent {

        @Test
        @DisplayName("успешно отвязывает студента от родителя")
        void success() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            Student student = Student.builder().id(STUDENT_ID).parent(parent).build();

            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

            service.unassignFromParent(STUDENT_ID);

            assertThat(student.getParent()).isNull();
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("у студента нет родителя — бросает ConflictException")
        void noParent_throwsConflict() {
            Student student = Student.builder().id(STUDENT_ID).parent(null).build();

            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> service.unassignFromParent(STUDENT_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " has no parent");

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("студент не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.unassignFromParent(STUDENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Student by id: " + STUDENT_ID + " not found");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно вызывает удаление из репозитория по id")
        void success() {
            when(studentRepository.existsById(STUDENT_ID)).thenReturn(true);

            service.delete(STUDENT_ID);

            verify(studentRepository).deleteById(STUDENT_ID);
        }
    }

    @Nested
    @DisplayName("handleUserDelete")
    class HandleUserDelete {

        @Test
        @DisplayName("если в событии есть роль STUDENT — запускает удаление")
        void roleMatches_callsDelete() {
            UserDeletedEvent event = new UserDeletedEvent(STUDENT_ID, Set.of(UserRole.STUDENT, UserRole.PARENT));
            when(studentRepository.existsById(STUDENT_ID)).thenReturn(true);

            service.handleUserDelete(event);

            verify(studentRepository).deleteById(STUDENT_ID);
        }

        @Test
        @DisplayName("если в событии нет роли STUDENT — ничего не делает")
        void roleMismatches_doesNothing() {
            UserDeletedEvent event = new UserDeletedEvent(STUDENT_ID, Set.of(UserRole.TEACHER, UserRole.PARENT));

            service.handleUserDelete(event);

            verify(studentRepository, never()).deleteById(any());
        }
    }
}
