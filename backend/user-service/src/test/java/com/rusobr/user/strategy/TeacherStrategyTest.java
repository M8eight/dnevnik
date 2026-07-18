package com.rusobr.user.strategy;

import com.rusobr.common.enums.UserRole;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.user.application.service.teacher.TeacherService;
import com.rusobr.user.application.service.user.strategy.TeacherStrategy;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
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
class TeacherStrategyTest {

    @Mock private TeacherService teacherService;
    @InjectMocks private TeacherStrategy strategy;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("getRole возвращает TEACHER")
    void getRole() {
        assertThat(strategy.getRole()).isEqualTo(UserRole.TEACHER);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("учитель мягко удалён — восстанавливает и обновляет поля")
        void softDeleted_restores() {
            Teacher teacher = mock(Teacher.class);
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            when(teacherService.findByIdWithDeleted(USER_ID)).thenReturn(Optional.of(teacher));

            strategy.save(USER_ID, details);

            verify(teacher).setDeletedAt(null);
            verify(teacher).setPhoneNumber("+7999");
            verify(teacher).setEmail("t@mail.com");
            verify(teacherService, never()).create(any(), any());
        }

        @Test
        @DisplayName("учитель не существует — создаёт нового")
        void notExists_creates() {
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            when(teacherService.findByIdWithDeleted(USER_ID)).thenReturn(Optional.empty());

            strategy.save(USER_ID, details);

            verify(teacherService).create(USER_ID, details);
        }

        @Test
        @DisplayName("передан неверный тип details — бросает ConflictException")
        void wrongDetails_throwsConflict() {
            assertThatThrownBy(() -> strategy.save(USER_ID, new StudentDetails("math")))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid teacher profile details");

            verifyNoInteractions(teacherService);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("делегирует удаление в teacherService")
        void success() {
            strategy.delete(USER_ID);
            verify(teacherService).delete(USER_ID);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно делегирует обновление в teacherService")
        void success() {
            TeacherDetails details = new TeacherDetails("t@mail.com", "+7999");
            strategy.update(USER_ID, details);
            verify(teacherService).update(USER_ID, details);
        }

        @Test
        @DisplayName("передан неверный тип details — бросает ConflictException")
        void wrongDetails_throwsConflict() {
            assertThatThrownBy(() -> strategy.update(USER_ID, new StudentDetails("math")))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid teacher profile details");

            verifyNoInteractions(teacherService);
        }
    }
}