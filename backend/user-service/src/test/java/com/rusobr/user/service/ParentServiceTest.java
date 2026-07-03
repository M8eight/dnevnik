package com.rusobr.user.service;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.ParentMapper;
import com.rusobr.user.application.service.parent.ParentService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParentServiceTest {

    @Mock private ParentRepository parentRepository;
    @Mock private ParentMapper parentMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks private ParentService service;

    private static final Long USER_ID = 1L;
    private static final Long PARENT_ID = 1L;

    @Nested
    @DisplayName("getWithUserById")
    class GetWithUserById {

        @Test
        @DisplayName("успешно возвращает ParentResponse, если родитель найден")
        void success() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            UserResponse userResponse = UserResponse.builder().id(USER_ID).build();
            ParentResponse expectedResponse = new ParentResponse(userResponse, Collections.emptySet());

            when(parentRepository.findWithUserById(PARENT_ID)).thenReturn(Optional.of(parent));
            when(parentMapper.toResponse(parent)).thenReturn(expectedResponse);

            ParentResponse result = service.getWithUserById(PARENT_ID);

            assertThat(result).isNotNull().isEqualTo(expectedResponse);
            verify(parentRepository).findWithUserById(PARENT_ID);
        }

        @Test
        @DisplayName("родитель не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(parentRepository.findWithUserById(PARENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getWithUserById(PARENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Parent by id: " + PARENT_ID + " not found");

            verifyNoInteractions(parentMapper);
        }
    }

    @Nested
    @DisplayName("getDetailsById")
    class GetDetailsById {

        @Test
        @DisplayName("успешно возвращает ParentDetails по id")
        void success() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            ParentDetails expectedDetails = new ParentDetails();

            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.of(parent));
            when(parentMapper.toParentDetails(parent)).thenReturn(expectedDetails);

            ParentDetails result = service.getDetailsById(PARENT_ID);

            assertThat(result).isNotNull();
            verify(parentRepository).findById(PARENT_ID);
        }

        @Test
        @DisplayName("родитель не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(parentRepository.findById(PARENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getDetailsById(PARENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Parent by id: " + PARENT_ID + " not found");

            verifyNoInteractions(parentMapper);
        }
    }

    @Nested
    @DisplayName("findByIdWithDeleted")
    class FindByIdWithDeleted {

        @Test
        @DisplayName("возвращает Optional с родителем, даже если он мягко удален")
        void found() {
            Parent parent = Parent.builder().id(PARENT_ID).build();
            when(parentRepository.findByIdWithDeleted(PARENT_ID)).thenReturn(Optional.of(parent));

            Optional<Parent> result = service.findByIdWithDeleted(PARENT_ID);

            assertThat(result).isPresent().contains(parent);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если родитель вообще не существует")
        void notFound_returnsEmptyOptional() {
            when(parentRepository.findByIdWithDeleted(PARENT_ID)).thenReturn(Optional.empty());

            Optional<Parent> result = service.findByIdWithDeleted(PARENT_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно находит пользователя и сохраняет нового родителя")
        void success() {
            User user = User.builder().id(USER_ID).build();
            ParentDetails details = new ParentDetails();
            Parent parentToSave = Parent.builder().user(user).build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(parentMapper.toEntity(user, details)).thenReturn(parentToSave);

            service.create(USER_ID, details);

            verify(parentRepository).save(parentToSave);
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException и не сохраняет родителя")
        void userNotFound_throwsException() {
            ParentDetails details = new ParentDetails();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User by id: " + USER_ID + " not found");

            verifyNoInteractions(parentMapper, parentRepository);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно проходит проверки существования (заглушка метода)")
        void success() {
            ParentDetails details = new ParentDetails();
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(parentRepository.existsById(USER_ID)).thenReturn(true);

            service.update(USER_ID, details);

            verify(userRepository).existsById(USER_ID);
            verify(parentRepository).existsById(USER_ID);
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException")
        void userNotFound_throwsException() {
            ParentDetails details = new ParentDetails();
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.update(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User by id: " + USER_ID + " not found");

            verify(parentRepository, never()).existsById(any());
        }

        @Test
        @DisplayName("пользователь есть, но родитель не найден — бросает NotFoundException")
        void parentNotFound_throwsException() {
            ParentDetails details = new ParentDetails();
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(parentRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.update(USER_ID, details))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Parent by id: " + USER_ID + " not found");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно вызывает удаление из репозитория по id")
        void success() {
            when(parentRepository.existsById(PARENT_ID)).thenReturn(true);

            service.delete(PARENT_ID);

            verify(parentRepository).deleteById(PARENT_ID);
        }
    }

    @Nested
    @DisplayName("handleUserDelete")
    class HandleUserDelete {

        @Test
        @DisplayName("если в событии есть роль PARENT — запускает удаление")
        void roleMatches_callsDelete() {
            UserDeletedEvent event = new UserDeletedEvent(PARENT_ID, Set.of(UserRole.PARENT, UserRole.STUDENT));
            when(parentRepository.existsById(PARENT_ID)).thenReturn(true);

            service.handleUserDelete(event);

            // Так как внутри handleUserDelete вызывает this.delete(parentId),
            // проверяем конечный эффект на репозитории
            verify(parentRepository).deleteById(PARENT_ID);
        }

        @Test
        @DisplayName("если в событии нет роли PARENT — ничего не делает")
        void roleMismatches_doesNothing() {
            UserDeletedEvent event = new UserDeletedEvent(PARENT_ID, Set.of(UserRole.STUDENT, UserRole.TEACHER));

            service.handleUserDelete(event);

            verify(parentRepository, never()).deleteById(any());
        }
    }
}