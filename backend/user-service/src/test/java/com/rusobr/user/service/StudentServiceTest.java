package com.rusobr.user.service;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.feignClient.SchoolClassClient;
import com.rusobr.user.infrastructure.mapper.StudentMapper;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.service.StudentService;
import com.rusobr.user.infrastructure.service.TeacherService;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentResponse;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private SchoolClassClient schoolClassClient;
    @Mock private StudentMapper studentMapper;
    @Mock private TeacherService teacherService;

    @InjectMocks private StudentService service;

    // Константы помогают избежать "магических чисел" и ошибок с ID
    private static final Long STUDENT_ID = 1L;
    private static final Long TEACHER_ID = 5L;

    @Nested
    @DisplayName("findBatchStudents")
    class FindBatchStudents {

        @Test
        @DisplayName("возвращает список студентов по ids")
        void success() {
            var ids = List.of(STUDENT_ID, 2L);
            var expectedResponse = List.of(
                    new StudentResponse(STUDENT_ID, "Иван", "Иванов", "kc-1"),
                    new StudentResponse(2L, "Мария", "Петрова", "kc-2")
            );

            when(studentRepository.findAllStudentsByIds(ids)).thenReturn(expectedResponse);

            List<StudentResponse> result = service.findBatchStudents(ids);

            assertThat(result).hasSize(2).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("null или пустые ids — возвращает пустой список")
        void emptyIds_returnsEmptyList() {
            assertThat(service.findBatchStudents(null)).isEmpty();
            assertThat(service.findBatchStudents(List.of())).isEmpty();

            verifyNoInteractions(studentRepository);
        }
    }

    @Nested
    @DisplayName("findStudentDetailById")
    class FindStudentDetailById {

        @Test
        @DisplayName("успешно возвращает детальный ответ")
        void success() {
            // Учитываем миграцию: student.id == user.id
            User user = new User();
            user.setId(STUDENT_ID);
            user.setFirstName("Иван");
            user.setLastName("Иванов");
            user.setKeycloakId("kc-1");

            Student student = Student.builder()
                    .id(STUDENT_ID)
                    .studyProfile("IT")
                    .build();
            student.setUser(user);

            SchoolClassResponse schoolClass = new SchoolClassResponse(100L, "10А", "2025-2026", TEACHER_ID);
            TeacherResponse teacher = new TeacherResponse(TEACHER_ID, "kc-t", "Анна", "Смирнова", "123", "a@a.ru");

            StudentResponseDetail expectedDetail = new StudentResponseDetail(
                    STUDENT_ID, STUDENT_ID, "kc-1", "Иван", "Иванов", "IT", schoolClass, teacher
            );

            // Настройка моков
            when(studentRepository.findWithUserById(STUDENT_ID)).thenReturn(Optional.of(student));
            when(schoolClassClient.getSchoolClassByStudentId(STUDENT_ID)).thenReturn(schoolClass);
            when(teacherService.findWithUserById(TEACHER_ID)).thenReturn(teacher);
            when(studentMapper.toStudentResponse(student, schoolClass, teacher)).thenReturn(expectedDetail);

            // Вызов
            StudentResponseDetail result = service.findStudentDetailById(STUDENT_ID);

            // Проверка
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(STUDENT_ID);
            assertThat(result.schoolClassTeacher().id()).isEqualTo(TEACHER_ID);
            verify(studentRepository).findWithUserById(STUDENT_ID);
        }

        @Test
        @DisplayName("студент не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(studentRepository.findWithUserById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findStudentDetailById(STUDENT_ID))
                    .isInstanceOf(NotFoundException.class);

            verifyNoInteractions(schoolClassClient, teacherService, studentMapper);
        }
    }

    @Nested
    @DisplayName("deleteStudentById")
    class DeleteStudentById {

        @Test
        @DisplayName("успешно удаляет студента")
        void success() {
            Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

            service.deleteStudentById(STUDENT_ID);

            verify(studentRepository).delete(student);
        }

        @Test
        @DisplayName("студент для удаления не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteStudentById(STUDENT_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(studentRepository, never()).delete(any());
        }
    }
}