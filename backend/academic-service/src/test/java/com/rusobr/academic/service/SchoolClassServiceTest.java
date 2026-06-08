package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.SchoolClassMapper;
import com.rusobr.academic.application.service.SchoolClassService;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolClassProjection;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.TeacherDetails;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    @Mock private SchoolClassRepository schoolClassRepository;
    @Mock private SchoolClassMapper schoolClassMapper;
    @Mock private UserClient userClient;
    @Mock private SchoolClassService self; // Для @Lazy селф-инъекции

    @InjectMocks private SchoolClassService service;

    private static final Long CLASS_ID = 1L;
    private static final Long STUDENT_ID = 42L;
    private static final Long TEACHER_ID = 7L;
    private static final String CLASS_NAME = "10A";
    private static final String SCHOOL_YEAR = "2026-2027";

    @BeforeEach
    void setUp() {
        // Решаем проблему с null в поле self
        ReflectionTestUtils.setField(service, "self", self);
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("успешно возвращает класс по ID")
        void success() {
            SchoolClass schoolClass = new SchoolClass();
            SchoolClassResponse response = new SchoolClassResponse(CLASS_ID, CLASS_NAME, SCHOOL_YEAR, TEACHER_ID);

            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(response);

            SchoolClassResponse result = service.findById(CLASS_ID);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("бросает NotFoundException, если класс не найден")
        void notFound() {
            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(CLASS_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found");
        }
    }

    @Nested
    @DisplayName("findWithClassStudentById")
    class FindWithClassStudentById {
        @Test
        @DisplayName("собирает полную информацию о классе, включая учеников и учителя")
        void success() {
            ClassStudent classStudent = ClassStudent.builder().studentId(STUDENT_ID).build();
            SchoolClass schoolClass = SchoolClass.builder()
                    .id(CLASS_ID)
                    .classTeacherId(TEACHER_ID)
                    .students(Set.of(classStudent))
                    .build();

            UserFeignResponse userResponse = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivan", "key");
            TeacherResponse teacherResponse = new TeacherResponse(
                    new UserFeignResponse(TEACHER_ID, "Петр", "Петров", "petr", "key"),
                    new TeacherDetails("petr@mail.com", "123")
            );
            SchoolClassFullResponse expectedResponse = mock(SchoolClassFullResponse.class);

            when(schoolClassRepository.findWithClassStudentById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(userClient.getBatchUsers(List.of(STUDENT_ID))).thenReturn(List.of(userResponse));
            when(userClient.getTeacherById(TEACHER_ID)).thenReturn(teacherResponse);
            when(schoolClassMapper.toSchoolClassFullResponse(schoolClass, List.of(userResponse), teacherResponse))
                    .thenReturn(expectedResponse);

            SchoolClassFullResponse result = service.findWithClassStudentById(CLASS_ID);

            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @DisplayName("findByStudentId")
    class FindByStudentId {
        @Test
        @DisplayName("возвращает класс по ID ученика")
        void success() {
            SchoolClassProjection projection = mock(SchoolClassProjection.class);
            SchoolClassResponse response = mock(SchoolClassResponse.class);

            when(schoolClassRepository.getSchoolClassByStudentId(STUDENT_ID)).thenReturn(Optional.of(projection));
            when(schoolClassMapper.toSchoolClassResponse(projection)).thenReturn(response);

            SchoolClassResponse result = service.findByStudentId(STUDENT_ID);

            assertThat(result).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("findAllClasses")
    class FindAllClasses {
        @Test
        @DisplayName("возвращает список всех классов")
        void success() {
            // 1. Мокаем интерфейс проекции, а не создаем сущность
            SchoolClassProjection projection = mock(SchoolClassProjection.class);
            SchoolClassResponse response = mock(SchoolClassResponse.class);

            // 2. Теперь типы совпадают: репозиторий возвращает список проекций
            when(schoolClassRepository.findAllByOrderByNameAsc()).thenReturn(List.of(projection));

            // 3. Настраиваем маппер на работу с проекцией
            when(schoolClassMapper.toSchoolClassResponse(projection)).thenReturn(response);

            List<SchoolClassResponse> result = service.findAllClasses();

            assertThat(result).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("assignTeacher & assignTeacherTransactional")
    class AssignTeacher {
        @Test
        @DisplayName("assignTeacher вызывает валидацию клиента и делегирует выполнение транзакционному методу")
        void assignTeacher_delegates() {
            service.assignTeacher(CLASS_ID, TEACHER_ID);

            verify(userClient).getTeacherById(TEACHER_ID);
            verify(self).assignTeacherTransactional(CLASS_ID, TEACHER_ID);
        }

        @Test
        @DisplayName("assignTeacherTransactional обновляет классного руководителя")
        void assignTeacherTransactional_success() {
            SchoolClass schoolClass = new SchoolClass();
            when(schoolClassRepository.findWithClassStudentById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            service.assignTeacherTransactional(CLASS_ID, TEACHER_ID);

            assertThat(schoolClass.getClassTeacherId()).isEqualTo(TEACHER_ID);
        }
    }

    @Nested
    @DisplayName("create & createTransactional")
    class Create {
        private final SchoolClassRequest request = new SchoolClassRequest(CLASS_NAME, SCHOOL_YEAR);

        @Test
        @DisplayName("create делегирует выполнение транзакционному методу")
        void create_delegates() {
            service.create(request);
            verify(self).createTransactional(request);
        }

        @Test
        @DisplayName("createTransactional успешно сохраняет новый класс")
        void createTransactional_success() {
            SchoolClass schoolClass = new SchoolClass();
            SchoolClassResponse response = mock(SchoolClassResponse.class);

            when(schoolClassRepository.existsByName(CLASS_NAME)).thenReturn(false);
            when(schoolClassMapper.toSchoolClass(request)).thenReturn(schoolClass);
            when(schoolClassRepository.save(schoolClass)).thenReturn(schoolClass);
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(response);

            SchoolClassResponse result = service.createTransactional(request);

            assertThat(result).isEqualTo(response);
            verify(schoolClassRepository).save(schoolClass);
        }

        @Test
        @DisplayName("бросает ConflictException, если имя уже занято")
        void createTransactional_conflict() {
            when(schoolClassRepository.existsByName(CLASS_NAME)).thenReturn(true);

            assertThatThrownBy(() -> service.createTransactional(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");

            verify(schoolClassRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update & updateTransactional")
    class Update {
        private final SchoolClassRequest request = new SchoolClassRequest(CLASS_NAME, SCHOOL_YEAR);

        @Test
        @DisplayName("update делегирует выполнение транзакционному методу")
        void update_delegates() {
            service.update(CLASS_ID, request);
            verify(self).updateTransactional(CLASS_ID, request);
        }

        @Test
        @DisplayName("updateTransactional успешно обновляет класс")
        void updateTransactional_success() {
            SchoolClass schoolClass = new SchoolClass();

            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(schoolClassRepository.existsByNameAndIdNot(CLASS_NAME, CLASS_ID)).thenReturn(false);

            service.updateTransactional(CLASS_ID, request);

            verify(schoolClassMapper).updateSchoolClass(schoolClass, request);
        }

        @Test
        @DisplayName("бросает ConflictException, если новое имя уже используется другим классом")
        void updateTransactional_conflict() {
            SchoolClass schoolClass = new SchoolClass();

            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(schoolClassRepository.existsByNameAndIdNot(CLASS_NAME, CLASS_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.updateTransactional(CLASS_ID, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");

            verify(schoolClassMapper, never()).updateSchoolClass(any(), any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("успешно удаляет класс")
        void success() {
            when(schoolClassRepository.existsById(CLASS_ID)).thenReturn(true);

            service.delete(CLASS_ID);

            verify(schoolClassRepository).deleteById(CLASS_ID);
        }

        @Test
        @DisplayName("бросает NotFoundException, если класс не найден")
        void notFound() {
            when(schoolClassRepository.existsById(CLASS_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(CLASS_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(schoolClassRepository, never()).deleteById(any());
        }
    }
}