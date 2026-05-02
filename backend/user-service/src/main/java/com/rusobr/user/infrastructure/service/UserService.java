package com.rusobr.user.infrastructure.service;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.CreateUserResponse;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakRestClient keycloakRestClient;
    private final UserMapper userMapper;

    public UserResponse findUserDbById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    public List<UserResponse> findBatchUsers(List<Long> ids) {
        return userRepository.findAllById(ids).stream().map(userMapper::toUserResponse).toList();
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest reqDto) {
        String keycloakUserId = keycloakRestClient.createKeyCloakUser(reqDto);

        try {
            User user = userMapper.toUser(reqDto);
            user.setKeycloakId(keycloakUserId);
            return userMapper.toCreateUserResponse(userRepository.save(user));
        } catch (Exception e) {
            keycloakRestClient.deleteKeyCloakUser(keycloakUserId);
            throw new RuntimeException("Keycloak could not be created");
        }
    }

    @Transactional
    public void deleteUser(String keycloakUserId) {
        keycloakRestClient.deleteKeyCloakUser(keycloakUserId);
        User user = userRepository.findByKeycloakId(keycloakUserId).orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.delete(user);
    }

    public List<KeycloakRole> getAllRoles() {
        return keycloakRestClient.getAllKeycloakRoles();
    }

    @Transactional
    public void assignRoleToUser(AssignRoleToUserRequest assignRoleToUserRequestDto) {
        keycloakRestClient.assignRoleToUser(assignRoleToUserRequestDto);
        User userDb = userRepository.findByKeycloakId(assignRoleToUserRequestDto.keycloakId()).orElseThrow(() -> new NotFoundException("User not found"));
        userDb.getRoles().add(assignRoleToUserRequestDto.roleName());
        userRepository.save(userDb);
    }

    @Transactional
    public void deleteRoleFromUser(AssignRoleToUserRequest assignRoleToUserRequestDto) {
        keycloakRestClient.deleteRoleFromUser(assignRoleToUserRequestDto);
        User userDb = userRepository.findByKeycloakId(assignRoleToUserRequestDto.keycloakId()).orElseThrow(() -> new RuntimeException("User not found"));
        userDb.getRoles().remove(assignRoleToUserRequestDto.roleName());
        userRepository.save(userDb);
    }

    //todo edit usr
}
