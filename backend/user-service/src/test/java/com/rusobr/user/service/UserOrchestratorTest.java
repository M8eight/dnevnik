package com.rusobr.user.service;

import com.rusobr.user.application.service.user.UserDbService;
import com.rusobr.user.application.service.user.UserOrchestrator;
import com.rusobr.user.application.service.user.UserService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import com.rusobr.user.web.dto.user.update.UserUpdateRequest;
import com.rusobr.user.web.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOrchestratorTest {

    @Mock private KeycloakRestClient keycloakRestClient;
    @Mock private UserDbService userDbService;
    @Mock private UserService userService;

    @InjectMocks private UserOrchestrator orchestrator;

    private static final Long USER_ID = 1L;
    private static final String KEYCLOAK_ID = "kc-123";
    private static final String KEYCLOAK_ROLE_ID = "role-id-1";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно создаёт пользователя в Keycloak и БД")
        void success() {
            UserDataDto userDataDto = mock(UserDataDto.class);

            StudentDetails details = new StudentDetails("math");
            UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(userDataDto, UserRole.STUDENT, details);
            KeycloakRole keycloakRole = new KeycloakRole(KEYCLOAK_ROLE_ID, "STUDENT");
            UserResponse expected = UserResponse.builder().id(USER_ID).build();

            when(keycloakRestClient.createKeyCloakUser(userDataDto)).thenReturn(KEYCLOAK_ID);
            when(keycloakRestClient.getRoleByName("STUDENT")).thenReturn(keycloakRole);
            when(userDbService.create(request, KEYCLOAK_ID)).thenReturn(expected);

            UserResponse result = orchestrator.create(request);

            assertThat(result).isEqualTo(expected);
            verify(keycloakRestClient).createKeyCloakUser(userDataDto);
            verify(keycloakRestClient).getRoleByName("STUDENT");
            verify(keycloakRestClient).assignRoleToUser(new AssignRoleToUserRequest(KEYCLOAK_ID, "STUDENT", KEYCLOAK_ROLE_ID));
            verify(userDbService).create(request, KEYCLOAK_ID);
        }

        @Test
        @DisplayName("ошибка при создании в БД — откатывает пользователя из Keycloak и бросает ConflictException")
        void dbFails_rollbackKeycloakUser() {
            UserDataDto userDataDto = mock(UserDataDto.class);
            when(userDataDto.username()).thenReturn("ivan");

            StudentDetails details = new StudentDetails("math");
            UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(userDataDto, UserRole.STUDENT, details);
            KeycloakRole keycloakRole = new KeycloakRole(KEYCLOAK_ROLE_ID, "STUDENT");

            when(keycloakRestClient.createKeyCloakUser(userDataDto)).thenReturn(KEYCLOAK_ID);
            when(keycloakRestClient.getRoleByName("STUDENT")).thenReturn(keycloakRole);
            when(userDbService.create(any(), any())).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> orchestrator.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Could not create user");

            verify(keycloakRestClient).deleteKeyCloakUser(KEYCLOAK_ID);
        }

        @Test
        @DisplayName("ошибка при назначении роли в Keycloak — откатывает пользователя и бросает ConflictException")
        void assignRoleFails_rollbackKeycloakUser() {
            UserDataDto userDataDto = mock(UserDataDto.class);
            when(userDataDto.username()).thenReturn("ivan");

            StudentDetails details = new StudentDetails("math");
            UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(userDataDto, UserRole.STUDENT, details);
            KeycloakRole keycloakRole = new KeycloakRole(KEYCLOAK_ROLE_ID, "STUDENT");

            when(keycloakRestClient.createKeyCloakUser(userDataDto)).thenReturn(KEYCLOAK_ID);
            when(keycloakRestClient.getRoleByName("STUDENT")).thenReturn(keycloakRole);
            doThrow(new RuntimeException("Keycloak error")).when(keycloakRestClient).assignRoleToUser(any());

            assertThatThrownBy(() -> orchestrator.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Could not create user");

            verify(keycloakRestClient).deleteKeyCloakUser(KEYCLOAK_ID);
            verifyNoInteractions(userDbService);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно обновляет пользователя в Keycloak и БД")
        void success() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            StudentDetails details = new StudentDetails("physics");
            UserUpdateData updateData = UserUpdateData.builder()
                    .username("new").firstName("New").lastName("Updated").build();
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, null,
                    Set.of(UserRole.STUDENT),
                    Map.of(UserRole.STUDENT, details)
            );
            UserResponse expected = UserResponse.builder().id(USER_ID).build();

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(userService.isUsernameTaken("new", USER_ID)).thenReturn(false);
            when(userDbService.update(eq(user), eq(updateData), eq(Set.of(UserRole.STUDENT)), any()))
                    .thenReturn(expected);

            UserResponse result = orchestrator.update(USER_ID, request);

            assertThat(result).isEqualTo(expected);
            verify(keycloakRestClient).updateKeycloakUserProfile(KEYCLOAK_ID, updateData);
            verify(userDbService).update(eq(user), eq(updateData), eq(Set.of(UserRole.STUDENT)), any());
        }

        @Test
        @DisplayName("username уже занят — бросает ConflictException без обновления")
        void usernameTaken_throwsConflict() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData updateData = UserUpdateData.builder().username("taken").build();
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, null,
                    Set.of(UserRole.STUDENT),
                    Map.of(UserRole.STUDENT, new StudentDetails("math"))
            );

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(userService.isUsernameTaken("taken", USER_ID)).thenReturn(true);

            assertThatThrownBy(() -> orchestrator.update(USER_ID, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Username taken already exists");

            verifyNoInteractions(keycloakRestClient, userDbService);
        }

        @Test
        @DisplayName("username == null — проверка занятости пропускается")
        void nullUsername_skipsValidation() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData updateData = UserUpdateData.builder().firstName("New").build();
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, null,
                    Set.of(UserRole.STUDENT),
                    Map.of(UserRole.STUDENT, new StudentDetails("math"))
            );

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(userDbService.update(any(), any(), any(), any()))
                    .thenReturn(UserResponse.builder().id(USER_ID).build());

            orchestrator.update(USER_ID, request);

            verify(userService, never()).isUsernameTaken(any(), any());
        }

        @Test
        @DisplayName("ошибка при обновлении в БД — откатывает профиль и роли в Keycloak, бросает ConflictException")
        void dbFails_rollbackKeycloak() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData updateData = UserUpdateData.builder().username("new").build();
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, null,
                    Set.of(UserRole.STUDENT),
                    Map.of(UserRole.STUDENT, new StudentDetails("math"))
            );

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(userService.isUsernameTaken("new", USER_ID)).thenReturn(false);
            when(userDbService.update(any(), any(), any(), any())).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> orchestrator.update(USER_ID, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Could not update user");

            // Откат: обновляем старыми данными
            UserUpdateData expectedOldData = UserUpdateData.builder()
                    .username("old").firstName("Old").lastName("Name").build();
            verify(keycloakRestClient).updateKeycloakUserProfile(KEYCLOAK_ID, expectedOldData);
        }

        @Test
        @DisplayName("пароль передан — вызывает resetKeycloakPassword после обновления")
        void passwordProvided_resetsPassword() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData updateData = UserUpdateData.builder().build();
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, "newPassword123",
                    Set.of(UserRole.STUDENT),
                    Map.of(UserRole.STUDENT, new StudentDetails("math"))
            );

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(userDbService.update(any(), any(), any(), any()))
                    .thenReturn(UserResponse.builder().id(USER_ID).build());

            orchestrator.update(USER_ID, request);

            verify(keycloakRestClient).resetKeycloakPassword(KEYCLOAK_ID, "newPassword123");
        }

        @Test
        @DisplayName("пароль null — resetKeycloakPassword не вызывается")
        void passwordNull_doesNotResetPassword() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData updateData = UserUpdateData.builder().build();
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, null,
                    Set.of(UserRole.STUDENT),
                    Map.of(UserRole.STUDENT, new StudentDetails("math"))
            );

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(userDbService.update(any(), any(), any(), any()))
                    .thenReturn(UserResponse.builder().id(USER_ID).build());

            orchestrator.update(USER_ID, request);

            verify(keycloakRestClient, never()).resetKeycloakPassword(any(), any());
        }

        @Test
        @DisplayName("роль убрана — удаляет её из Keycloak; роль добавлена — назначает в Keycloak")
        void roleChanges_syncKeycloakRoles() {
            User user = User.builder()
                    .id(USER_ID).keycloakId(KEYCLOAK_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT, UserRole.TEACHER)))
                    .build();

            UserUpdateData updateData = UserUpdateData.builder().build();
            // Убираем TEACHER, добавляем PARENT
            UserUpdateRequest request = new UserUpdateRequest(
                    USER_ID, updateData, null,
                    Set.of(UserRole.STUDENT, UserRole.PARENT),
                    Map.of(
                            UserRole.STUDENT, new StudentDetails("math"),
                            UserRole.PARENT, new StudentDetails(null)
                    )
            );

            KeycloakRole teacherRole = new KeycloakRole("teacher-role-id", "TEACHER");
            KeycloakRole parentRole = new KeycloakRole("parent-role-id", "PARENT");

            when(userService.getByIdInternal(USER_ID)).thenReturn(user);
            when(keycloakRestClient.getRoleByName("TEACHER")).thenReturn(teacherRole);
            when(keycloakRestClient.getRoleByName("PARENT")).thenReturn(parentRole);
            when(userDbService.update(any(), any(), any(), any()))
                    .thenReturn(UserResponse.builder().id(USER_ID).build());

            orchestrator.update(USER_ID, request);

            verify(keycloakRestClient).deleteRoleFromUser(
                    new AssignRoleToUserRequest(KEYCLOAK_ID, "TEACHER", "teacher-role-id"));
            verify(keycloakRestClient).assignRoleToUser(
                    new AssignRoleToUserRequest(KEYCLOAK_ID, "PARENT", "parent-role-id"));
        }
    }
}