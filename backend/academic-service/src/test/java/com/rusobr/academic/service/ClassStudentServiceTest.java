package com.rusobr.academic.service;

import com.rusobr.academic.application.service.ClassStudentService;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.ClassStudentRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassStudentServiceTest {

    @Mock private SchoolClassRepository schoolClassRepository;
    @Mock private ClassStudentRepository classStudentRepository;
    @Mock private UserClient userClient;
    @Mock private ClassStudentService self;

    @InjectMocks private ClassStudentService service;

    private static final Long CLASS_ID = 1L;
    private static final Long STUDENT_ID = 42L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", self);
    }

    @Nested
    @DisplayName("getUnassignedStudents")
    class GetUnassignedStudents {

        @Test
        @DisplayName("успешно возвращает список нераспределенных студентов")
        void success() {
            Set<Long> assignedIds = Set.of(10L, 20L);
            UserFeignResponse studentResponse = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivan", "keycloak-id");
            List<UserFeignResponse> expectedList = List.of(studentResponse);

            when(classStudentRepository.findAllStudentIds()).thenReturn(assignedIds);
            when(userClient.getBatchStudentsExcludeAssigned(assignedIds)).thenReturn(expectedList);

            List<UserFeignResponse> result = service.getUnassignedStudents();

            assertThat(result).isNotNull().isEqualTo(expectedList);
            verify(classStudentRepository).findAllStudentIds();
            verify(userClient).getBatchStudentsExcludeAssigned(assignedIds);
        }
    }

    @Nested
    @DisplayName("addStudent")
    class AddStudent {

        @Test
        @DisplayName("успешно проверяет студента во внешнем клиенте и делегирует вызов в транзакционный метод")
        void success() {
            // В addStudent() сначала дергается feign-клиент, а затем метод через self-прокси
            doNothing().when(userClient).getStudentById(STUDENT_ID);

            service.addStudent(CLASS_ID, STUDENT_ID);

            verify(userClient).getStudentById(STUDENT_ID);
            verify(self).addStudentTransactional(CLASS_ID, STUDENT_ID);
        }
    }

    @Nested
    @DisplayName("addStudentTransactional")
    class AddStudentTransactional {

        @Test
        @DisplayName("школьный класс не найден — бросает NotFoundException")
        void classNotFound_throwsNotFoundException() {
            when(schoolClassRepository.existsById(CLASS_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.addStudentTransactional(CLASS_ID, STUDENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: " + CLASS_ID);

            verify(classStudentRepository, never()).existsByStudentId(any());
            verify(classStudentRepository, never()).save(any());
        }

        @Test
        @DisplayName("студент уже привязан к классу — бросает ConflictException")
        void studentAlreadyExists_throwsConflictException() {
            when(schoolClassRepository.existsById(CLASS_ID)).thenReturn(true);
            when(classStudentRepository.existsByStudentId(STUDENT_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.addStudentTransactional(CLASS_ID, STUDENT_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Student already exists in class");

            verify(schoolClassRepository, never()).getReferenceById(any());
            verify(classStudentRepository, never()).save(any());
        }

        @Test
        @DisplayName("успешно находит класс, проверяет дубликаты и сохраняет студента в класс")
        void success() {
            SchoolClass schoolClass = mock(SchoolClass.class);

            when(schoolClassRepository.existsById(CLASS_ID)).thenReturn(true);
            when(classStudentRepository.existsByStudentId(STUDENT_ID)).thenReturn(false);
            when(schoolClassRepository.getReferenceById(CLASS_ID)).thenReturn(schoolClass);

            service.addStudentTransactional(CLASS_ID, STUDENT_ID);

            verify(classStudentRepository).save(argThat(classStudent ->
                    classStudent.getStudentId().equals(STUDENT_ID) &&
                            classStudent.getSchoolClass() == schoolClass
            ));
        }
    }

    @Nested
    @DisplayName("removeStudent")
    class RemoveStudent {

        @Test
        @DisplayName("связь класса и студента не найдена — бросает NotFoundException")
        void classStudentNotFound_throwsNotFoundException() {
            when(classStudentRepository.findBySchoolClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeStudent(CLASS_ID, STUDENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("ClassStudent Not Found by classId: " + CLASS_ID + " and studentId: " + STUDENT_ID);

            verify(classStudentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("успешно находит связь и удаляет студента из класса")
        void success() {
            ClassStudent classStudent = ClassStudent.builder().id(77L).studentId(STUDENT_ID).build();
            when(classStudentRepository.findBySchoolClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                    .thenReturn(Optional.of(classStudent));

            service.removeStudent(CLASS_ID, STUDENT_ID);

            verify(classStudentRepository).delete(classStudent);
        }
    }
}