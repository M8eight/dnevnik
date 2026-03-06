package com.rusobr.user.infrastructure.service;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.webClient.KeycloackRestClient;
import com.rusobr.user.web.dto.keycloack.KeycloackUserResponse;
import com.rusobr.user.web.dto.keycloack.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloack.KeycloackUserRequest;
import com.rusobr.user.web.dto.keycloack.role.KeycloackRoleDto;
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
    private final KeycloackRestClient keycloackRestClient;
    private final UserMapper userMapper;

    public UserResponse findUserDbById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }


    @Transactional
    public KeycloackUserResponse createUser(KeycloackUserRequest reqDto) {
        String keycloackUserId = keycloackRestClient.createKeyCloackUser(
                KeycloackUserRequest.builder()
                        .username(reqDto.username())
                        .password(reqDto.password())
                        .build()
        );

        try {
            User user = userMapper.toUser(reqDto);
            user.setKeycloackId(keycloackUserId);
            return userMapper.toKeycloackUserResponse(userRepository.save(user));
        } catch (Exception e) {
            keycloackRestClient.deleteKeyCloackUser(keycloackUserId);
            throw new RuntimeException("Keycloack could not be created");
        }
    }

    @Transactional
    public void deleteUser(String keycloackUserId) {
        keycloackRestClient.deleteKeyCloackUser(keycloackUserId);
        User user = userRepository.findByKeycloackId(keycloackUserId).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public List<KeycloackRoleDto> getAllRoles() {
        return keycloackRestClient.getAllKeycloackRoles();
    }

    @Transactional
    public void assignRoleToUser(AssignRoleToUserRequest assignRoleToUserRequestDto) {
        keycloackRestClient.assignRoleToUser(assignRoleToUserRequestDto);
        User userDb = userRepository.findByKeycloackId(assignRoleToUserRequestDto.keycloackId()).orElseThrow(() -> new RuntimeException("User not found"));
        userDb.getRoles().add(assignRoleToUserRequestDto.roleName());
        userRepository.save(userDb);
    }

    @Transactional
    public void deleteRoleFromUser(AssignRoleToUserRequest assignRoleToUserRequestDto) {
        keycloackRestClient.deleteRoleFromUser(assignRoleToUserRequestDto);
        User userDb = userRepository.findByKeycloackId(assignRoleToUserRequestDto.keycloackId()).orElseThrow(() -> new RuntimeException("User not found"));
        userDb.getRoles().remove(assignRoleToUserRequestDto.roleName());
        userRepository.save(userDb);
    }

    //todo edit usr
}
