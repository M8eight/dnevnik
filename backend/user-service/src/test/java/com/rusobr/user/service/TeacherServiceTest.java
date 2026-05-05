package com.rusobr.user.service;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.TeacherMapper;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock private TeacherRepository teacherRepository;
    @Mock private TeacherMapper teacherMapper;

    @InjectMocks private TeacherService teacherService;

    private final Long TEACHER_ID = 1L;

    @Test
    @DisplayName("findWithUserById: успешно возвращает учителя")
    void findWithUserById_Success() {
        // Arrange
        Teacher teacher = new Teacher(); // Предположим, там есть нужные поля
        TeacherResponse expectedResponse = new TeacherResponse(
                TEACHER_ID, "kc-id", "Анна", "Смирнова", "+7999", "anna@mail.ru"
        );

        when(teacherRepository.findWithUserById(TEACHER_ID)).thenReturn(Optional.of(teacher));
        when(teacherMapper.toTeacherResponse(teacher)).thenReturn(expectedResponse);

        // Act
        TeacherResponse result = teacherService.findWithUserById(TEACHER_ID);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(teacherRepository).findWithUserById(TEACHER_ID);
    }

    @Test
    @DisplayName("findWithUserById: бросает исключение, если учитель не найден")
    void findWithUserById_NotFound() {
        when(teacherRepository.findWithUserById(TEACHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.findWithUserById(TEACHER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Teacher with id " + TEACHER_ID + " not found");
    }

    @Test
    @DisplayName("deleteById: вызывает удаление в репозитории")
    void deleteById_Success() {
        teacherService.deleteById(TEACHER_ID);
        verify(teacherRepository).deleteById(TEACHER_ID);
    }
}