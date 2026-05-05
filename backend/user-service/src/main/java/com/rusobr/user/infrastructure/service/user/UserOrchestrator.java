package com.rusobr.user.infrastructure.service.user;

import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrchestrator {

    private final KeycloakRestClient keycloakRestClient;
    private final UserDbService userDbService;

    public UserResponse create(UserRequest<? extends UserProfileDetails> createUserRequest) {
        //Создаем пользователя в keycloak и ищем роль и привязываем к пользователю
        String kId = keycloakRestClient.createKeyCloakUser(createUserRequest.user());
        KeycloakRole kRole = keycloakRestClient.getRoleByName(createUserRequest.role().name());
        keycloakRestClient.assignRoleToUser(new AssignRoleToUserRequest(kId, kRole.name(), kRole.id()));

        UserResponse userResponse;
        try {
            userResponse = userDbService.createUserDb(createUserRequest, kId);
        } catch (Exception e) {
            log.error("Rollback create User {}", createUserRequest.user().firstName());
            keycloakRestClient.deleteKeyCloakUser(kId);
            throw new ConflictException("Could not create user");
        }
        return userResponse;
    }
}
