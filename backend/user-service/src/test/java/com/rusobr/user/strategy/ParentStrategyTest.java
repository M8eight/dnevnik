package com.rusobr.user.strategy;

import com.rusobr.common.enums.UserRole;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.user.application.service.parent.ParentService;
import com.rusobr.user.application.service.user.strategy.ParentStrategy;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
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
class ParentStrategyTest {

    @Mock private ParentService parentService;
    @InjectMocks private ParentStrategy strategy;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("getRole возвращает PARENT")
    void getRole() {
        assertThat(strategy.getRole()).isEqualTo(UserRole.PARENT);

    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("родитель мягко удалён — восстанавливает через setDeletedAt(null)")
        void softDeleted_restores() {
            Parent parent = mock(Parent.class);
            when(parentService.findByIdWithDeleted(USER_ID)).thenReturn(Optional.of(parent));

            strategy.save(USER_ID, new ParentDetails());

            verify(parent).setDeletedAt(null);
            verify(parentService, never()).create(any(), any());
        }

        @Test
        @DisplayName("родитель не существует — создаёт нового")
        void notExists_creates() {
            ParentDetails details = new ParentDetails();
            when(parentService.findByIdWithDeleted(USER_ID)).thenReturn(Optional.empty());

            strategy.save(USER_ID, details);

            verify(parentService).create(USER_ID, details);
        }

        @Test
        @DisplayName("передан неверный тип details — бросает ConflictException")
        void wrongDetails_throwsConflict() {
            StudentDetails wrongDetails = new StudentDetails("math");

            assertThatThrownBy(() -> strategy.save(USER_ID, wrongDetails))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid parent profile details");

            verifyNoInteractions(parentService);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("делегирует удаление в parentService")
        void success() {
            strategy.delete(USER_ID);
            verify(parentService).delete(USER_ID);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно делегирует обновление в parentService")
        void success() {
            ParentDetails details = new ParentDetails();
            strategy.update(USER_ID, details);
            verify(parentService).update(USER_ID, details);
        }

        @Test
        @DisplayName("передан неверный тип details — бросает ConflictException")
        void wrongDetails_throwsConflict() {
            assertThatThrownBy(() -> strategy.update(USER_ID, new StudentDetails("math")))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid parent profile details");

            verifyNoInteractions(parentService);
        }
    }
}