package com.rusobr.user.application.service.user;

import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserProfileDetails;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import com.rusobr.user.web.dto.user.update.UserUpdateRequest;
import com.rusobr.user.web.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.rusobr.user.web.exception.ExceptionCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrchestrator {

    private final KeycloakRestClient keycloakRestClient;
    private final UserDbService userDbService;
    private final UserService userService;

    public void create(UserCreateRequest<? extends UserProfileDetails> userCreateRequest) {
        UserResponse user = userDbService.create(userCreateRequest);

        String kId = null;
        try {
            kId = keycloakRestClient.createKeyCloakUser(userCreateRequest.user(), user.id());
            KeycloakRole kRole = keycloakRestClient.getRoleByName(userCreateRequest.role().name());
            keycloakRestClient.assignRoleToUser(new AssignRoleToUserRequest(kId, kRole.name(), kRole.id()));

            userService.setKeycloakId(kId, user.id());

        } catch (Exception e) {
            log.error("Rollback create user: {} keycloakId: {}", user, kId, e);
            if (kId != null) {
                try {
                    keycloakRestClient.deleteKeyCloakUser(kId);
                } catch (Exception ex) {
                    log.error("Could not delete keycloak user", ex);
                }
            }
            userService.deleteUserCascade(user.id());
            throw new ConflictException("Could not create user", USER_CREATE_CONFLICT);
        }
    }

    public UserResponse update(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userService.getByIdInternal(userId);
        validateUsername(userUpdateRequest.user().username(), userId);

        //Собираем старые данные пользователя на случай отката
        UserUpdateData oldUserResponse = getOldData(user);
        Set<UserRole> currentRoles = new HashSet<>(user.getRoles());
        Set<UserRole> newRoles = userUpdateRequest.roles();

        //Обновляем профиль пользователя
        keycloakRestClient.updateKeycloakUserProfile(user.getKeycloakId(), userUpdateRequest.user());
        updateKeycloakRoles(user, currentRoles, newRoles);

        UserResponse userResponse;
        try {
            userResponse = userDbService.update(user, userUpdateRequest.user(), userUpdateRequest.roles(),
                    userUpdateRequest.details());

            Optional.ofNullable(userUpdateRequest.password())
                    .ifPresent(p -> keycloakRestClient.resetKeycloakPassword(user.getKeycloakId(), p));

            return userResponse;
        } catch (Exception e) {
            log.error("Rollback update user {}", user.getUsername());
            //В случае отката обновляем данные старыми данными
            keycloakRestClient.updateKeycloakUserProfile(user.getKeycloakId(), oldUserResponse);
            //Что бы вернуть данные мы должны поменять местами current и new роли
            updateKeycloakRoles(user, newRoles, currentRoles);

            throw new ConflictException("Could not update user", USER_UPDATE_CONFLICT);
        }
    }

    private void validateUsername(String username, Long id) {

        if (username == null) {
            return;
        }

        if (userService.isUsernameTaken(username, id)) {
            throw new ConflictException("Username %s already exists".formatted(username), USERNAME_ALREADY_EXISTS);
        }
    }

    private UserUpdateData getOldData(User user) {
        return UserUpdateData.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName()).build();
    }

    private void updateKeycloakRoles(User user, Set<UserRole> currentRoles, Set<UserRole> newRoles) {
        currentRoles.stream()
                .filter(currentRole -> !newRoles.contains(currentRole))
                .forEach(role -> {
                    KeycloakRole kRole = keycloakRestClient.getRoleByName(role.name());
                    keycloakRestClient.deleteRoleFromUser(new AssignRoleToUserRequest(user.getKeycloakId(), kRole.name(), kRole.id()));
                });
        newRoles.stream()
                .filter(newRole -> !currentRoles.contains(newRole))
                .forEach(role -> {
                    KeycloakRole kRole = keycloakRestClient.getRoleByName(role.name());
                    keycloakRestClient.assignRoleToUser(new AssignRoleToUserRequest(user.getKeycloakId(), kRole.name(), kRole.id()));
                });
    }

}
