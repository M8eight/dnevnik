package com.rusobr.user.service;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRoles;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.service.UserService;
import com.rusobr.user.infrastructure.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.CreateUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private KeycloakRestClient keycloakRestClient;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    private final String KC_ID = "kc-uuid";

    @Nested
    @DisplayName("createUser tests")
    class CreateUserTests {

        @Test
        @DisplayName("успешное создание: Keycloak -> DB")
        void createUser_Success() {
            var req = new CreateUserRequest("test", "user", "pass", "test@test.com");
            var userEntity = new User();
            var response = CreateUserResponse.builder().keycloakId(KC_ID).build();

            when(keycloakRestClient.createKeyCloakUser(req)).thenReturn(KC_ID);
            when(userMapper.toUser(req)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            when(userMapper.toCreateUserResponse(userEntity)).thenReturn(response);

            var result = userService.createUser(req);

            assertThat(result.keycloakId()).isEqualTo(KC_ID);
            verify(userRepository).save(userEntity);
            assertThat(userEntity.getKeycloakId()).isEqualTo(KC_ID);
        }

        @Test
        @DisplayName("ошибка БД: должен вызвать удаление из Keycloak")
        void createUser_DatabaseFailure_ShouldCompensate() {
            var req = new CreateUserRequest("test", "user", "pass", "test@test.com");
            when(keycloakRestClient.createKeyCloakUser(req)).thenReturn(KC_ID);
            when(userMapper.toUser(req)).thenReturn(new User());

            // Имитируем падение БД
            when(userRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

            assertThatThrownBy(() -> userService.createUser(req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Keycloak could not be created");

            // Проверяем компенсацию (удаление из KC)
            verify(keycloakRestClient).deleteKeyCloakUser(KC_ID);
        }
    }

    @Nested
    @DisplayName("role operations")
    class RoleTests {

        @Test
        @DisplayName("назначение роли: Keycloak -> DB")
        void assignRole_Success() {
            var req = new AssignRoleToUserRequest(KC_ID, UserRoles.TEACHER, "role-123");
            var user = new User();
            user.setRoles(new HashSet<>());

            when(userRepository.findByKeycloakId(KC_ID)).thenReturn(Optional.of(user));

            userService.assignRoleToUser(req);

            verify(keycloakRestClient).assignRoleToUser(req);
            assertThat(user.getRoles()).contains(UserRoles.TEACHER);
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("read operations")
    class ReadTests {

        @Test
        @DisplayName("findAll: маппинг страницы")
        void findAll_Success() {
            var pageable = PageRequest.of(0, 10);
            var user = new User();
            var userPage = new PageImpl<>(List.of(user));
            var userResponse = new UserResponse("Ivan", "Ivanov", KC_ID, 1L);

            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            var result = userService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).firstName()).isEqualTo("Ivan");
        }
    }
}