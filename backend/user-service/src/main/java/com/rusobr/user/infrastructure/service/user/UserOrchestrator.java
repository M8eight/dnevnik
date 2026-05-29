package com.rusobr.user.infrastructure.service.user;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import com.rusobr.user.web.dto.user.update.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrchestrator {

    private final KeycloakRestClient keycloakRestClient;
    private final UserDbService userDbService;
    private final UserService userService;

    public UserResponse create(UserCreateRequest<? extends UserProfileDetails> userCreateRequest) {
        //Создаем пользователя в keycloak и ищем роль и привязываем к пользователю
        String kId = keycloakRestClient.createKeyCloakUser(userCreateRequest.user());
        KeycloakRole kRole = keycloakRestClient.getRoleByName(userCreateRequest.role().name());
        keycloakRestClient.assignRoleToUser(new AssignRoleToUserRequest(kId, kRole.name(), kRole.id()));

        UserResponse userResponse;
        try {
            userResponse = userDbService.createUserDb(userCreateRequest, kId);
        } catch (Exception e) {
            log.error("Rollback create user {}", userCreateRequest.user().username());
            keycloakRestClient.deleteKeyCloakUser(kId);
            throw new ConflictException("Could not create user");
        }
        return userResponse;
    }

    public UserResponse update(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userService.findByIdInternal(userId);

        validUsername(userUpdateRequest.user().username(), userId);

        //Собираем старые данные пользователя на случай отката
        UserUpdateData oldUserResponse = getOldUserData(user);

        Set<UserRole> currentRoles = new HashSet<>(user.getRoles());
        Set<UserRole> newRoles = userUpdateRequest.roles();

        //Обновляем профиль пользователя, далее обновляем пароль если пришел новый
        keycloakRestClient.updateKeycloakUserProfile(user.getKeycloakId(), userUpdateRequest.user());
        updateKeycloakUserRoles(user, currentRoles, newRoles);
        Optional.ofNullable(userUpdateRequest.password())
                .ifPresent(p -> keycloakRestClient.resetKeycloakPassword(user.getKeycloakId(), p));

        UserResponse userResponse;
        try {
            userResponse = userDbService.updateUserDb(user, userUpdateRequest.user(), userUpdateRequest.roles(), userUpdateRequest.details());
        } catch (Exception e) {
            log.error("Rollback update user {}", user.getUsername());
            //В случае отката обновляем данные старыми данными
            keycloakRestClient.updateKeycloakUserProfile(user.getKeycloakId(), oldUserResponse);
            //Что бы вернуть данные мы должны поменять местами current и new роли
            updateKeycloakUserRoles(user, newRoles, currentRoles);

            throw new ConflictException("Could not update user");
        }

        return userResponse;
    }

    private void validUsername(String username, Long id) {

        if (username == null) {
            return;
        }

        if (userService.isAlreadyExistsByUsername(username, id)) {
            throw new ConflictException("Username already exists");
        }
    }

    private UserUpdateData getOldUserData(User user) {
        return UserUpdateData.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName()).build();
    }

    private void updateKeycloakUserRoles(User user, Set<UserRole> currentRoles, Set<UserRole> newRoles) {
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
