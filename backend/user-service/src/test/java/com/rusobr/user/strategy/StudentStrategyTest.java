package com.rusobr.user.strategy;

import com.rusobr.user.application.service.student.StudentService;
import com.rusobr.user.application.service.user.strategy.StudentStrategy;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentStrategyTest {

    @Mock private StudentService studentService;
    @InjectMocks private StudentStrategy strategy;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("getRole возвращает STUDENT")
    void getRole() {
        assertThat(strategy.getRole()).isEqualTo(UserRole.STUDENT);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("студент мягко удалён — восстанавливает и обновляет studyProfile")
        void softDeleted_restores() {
            Student student = mock(Student.class);
            StudentDetails details = new StudentDetails("physics");
            when(studentService.findByIdWithDeleted(USER_ID)).thenReturn(Optional.of(student));

            strategy.save(USER_ID, details);

            verify(student).setDeletedAt(null);
            verify(student).setStudyProfile("physics");
            verify(studentService, never()).create(any(), any());
        }

        @Test
        @DisplayName("студент не существует — создаёт нового")
        void notExists_creates() {
            StudentDetails details = new StudentDetails("math");
            when(studentService.findByIdWithDeleted(USER_ID)).thenReturn(Optional.empty());

            strategy.save(USER_ID, details);

            verify(studentService).create(USER_ID, details);
        }

        @Test
        @DisplayName("передан неверный тип details — бросает ConflictException")
        void wrongDetails_throwsConflict() {
            assertThatThrownBy(() -> strategy.save(USER_ID, new ParentDetails()))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid user profile details");

            verifyNoInteractions(studentService);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("делегирует удаление в studentService")
        void success() {
            strategy.delete(USER_ID);
            verify(studentService).delete(USER_ID);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно делегирует обновление в studentService")
        void success() {
            StudentDetails details = new StudentDetails("math");
            strategy.update(USER_ID, details);
            verify(studentService).update(USER_ID, details);
        }

        @Test
        @DisplayName("передан неверный тип details — бросает ConflictException")
        void wrongDetails_throwsConflict() {
            assertThatThrownBy(() -> strategy.update(USER_ID, new ParentDetails()))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid user profile details for update");

            verifyNoInteractions(studentService);
        }
    }
}