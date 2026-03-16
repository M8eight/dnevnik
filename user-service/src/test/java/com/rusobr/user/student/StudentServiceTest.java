package com.rusobr.user.student;

import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.service.StudentService;
import com.rusobr.user.web.dto.student.StudentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("Возвращает batch запрос с пользователями")
    void shouldReturnBatchStudents() {
        List<Long> req = List.of(1L, 2L);
        List<StudentResponse> students = List.of(new StudentResponse(1L, "Алексей", "Кочетыгов"), new StudentResponse(2L, "Наталья", "Анатольевна"));

        when(studentRepository.findAllStudentsByIds(req)).thenReturn(students);

        List<StudentResponse> res = studentService.findBatchStudents(req);
        assertEquals(students.size(), res.size());
        assertEquals(students.get(1).firstname(), res.get(1).firstname());
    }

    @Test
    @DisplayName("Должен вернуть пустой массив")
    void shouldReturnEmptyList() {
        List<Long> req = List.of();

        List<StudentResponse> res = studentService.findBatchStudents(req);

        assertEquals(0, res.size());

        verifyNoInteractions(studentRepository);
    }
}
