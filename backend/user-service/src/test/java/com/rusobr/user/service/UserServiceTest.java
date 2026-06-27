package com.rusobr.user.service;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.application.service.user.UserService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.webClient.KeycloakRestClient;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private KeycloakRestClient keycloakRestClient;
    @Mock private UserMapper userMapper;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks private UserService service;

    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("успешно возвращает пользователя по id")
        void success() {
            User user = User.builder().id(USER_ID).build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            User result = service.getByIdInternal(USER_ID);

            assertThat(result).isNotNull().isEqualTo(user);
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException")
        void notFound_throwsException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getByIdInternal(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found with id: " + USER_ID);
        }
    }

    @Nested
    @DisplayName("isUsernameTaken")
    class IsUsernameTaken {

        @Test
        @DisplayName("username занят другим пользователем — возвращает true")
        void taken_returnsTrue() {
            when(userRepository.existsByUsernameAndIdNot("ivan", USER_ID)).thenReturn(true);

            boolean result = service.isUsernameTaken("ivan", USER_ID);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("username свободен — возвращает false")
        void free_returnsFalse() {
            when(userRepository.existsByUsernameAndIdNot("ivan", USER_ID)).thenReturn(false);

            boolean result = service.isUsernameTaken("ivan", USER_ID);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllByFilter")
    class GetAllByFilter {

        @Test
        @DisplayName("возвращает страницу UserResponse с применением фильтров")
        void success() {
            Pageable pageable = PageRequest.of(0, 10);
            User user = User.builder().id(USER_ID).build();
            UserResponse userResponse = UserResponse.builder().id(USER_ID).build();
            Page<User> userPage = new PageImpl<>(List.of(user));

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            Page<UserResponse> result = service.getAllByFilter(pageable, UserRole.STUDENT, "Ivan");

            assertThat(result.getContent()).hasSize(1).containsExactly(userResponse);
            verify(userRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("возвращает пустую страницу, если пользователи не найдены")
        void emptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<UserResponse> result = service.getAllByFilter(pageable, null, null);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно создаёт пользователя и возвращает UserResponse")
        void success() {
            UserDataDto dto = mock(UserDataDto.class);
            User user = User.builder().id(USER_ID).build();
            UserResponse expected = UserResponse.builder().id(USER_ID).build();

            when(userMapper.toUser(dto, "kc-123", Collections.singleton(UserRole.STUDENT))).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toCreateUserResponse(user)).thenReturn(expected);

            UserResponse result = service.create(dto, "kc-123", UserRole.STUDENT);

            assertThat(result).isEqualTo(expected);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("роль оборачивается в singleton-коллекцию перед передачей в маппер")
        void roleWrappedInSingleton() {
            UserDataDto dto = mock(UserDataDto.class);
            User user = User.builder().id(USER_ID).build();

            when(userMapper.toUser(any(), any(), any())).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toCreateUserResponse(user)).thenReturn(UserResponse.builder().id(USER_ID).build());

            service.create(dto, "kc-123", UserRole.TEACHER);

            verify(userMapper).toUser(dto, "kc-123", Collections.singleton(UserRole.TEACHER));
        }
    }

    @Nested
    @DisplayName("getAllRoles")
    class GetAllRoles {

        @Test
        @DisplayName("возвращает список ролей из Keycloak")
        void success() {
            List<KeycloakRole> roles = List.of(new KeycloakRole("id-1", "STUDENT"), new KeycloakRole("id-2", "TEACHER"));
            when(keycloakRestClient.getAllKeycloakRoles()).thenReturn(roles);

            List<KeycloakRole> result = service.getAllRoles();

            assertThat(result).hasSize(2).isEqualTo(roles);
            verify(keycloakRestClient).getAllKeycloakRoles();
        }
    }

    @Nested
    @DisplayName("deleteUserCascade")
    class DeleteUserCascade {

        @Test
        @DisplayName("успешно удаляет пользователя и публикует UserDeletedEvent")
        void success() {
            Set<UserRole> roles = Set.of(UserRole.STUDENT);
            User user = User.builder().id(USER_ID).roles(new java.util.HashSet<>(roles)).build();

            when(userRepository.findWithRolesById(USER_ID)).thenReturn(Optional.of(user));

            service.deleteUserCascade(USER_ID);

            verify(userRepository).delete(user);

            ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

            UserDeletedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.id()).isEqualTo(USER_ID);
            assertThat(publishedEvent.roles()).containsExactlyInAnyOrderElementsOf(roles);
        }

        @Test
        @DisplayName("пользователь не найден — бросает NotFoundException, событие не публикуется")
        void notFound_throwsException() {
            when(userRepository.findWithRolesById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteUserCascade(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found with id: " + USER_ID);

            verify(userRepository, never()).delete(any(User.class));
            verify(applicationEventPublisher, never()).publishEvent(any());
        }
    }
}