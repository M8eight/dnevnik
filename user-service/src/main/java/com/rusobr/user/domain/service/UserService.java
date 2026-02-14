package com.rusobr.user.domain.service;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.webClient.KeycloackRestClient;
import com.rusobr.user.web.dto.AssignRoleToUserRequestDto;
import com.rusobr.user.web.dto.CreateKeyCloackUserRequestDto;
import com.rusobr.user.web.dto.CreateUserDtoRequest;
import com.rusobr.user.web.dto.CreateUserDtoResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloackRestClient keycloackRestClient;
    private final UserMapper userMapper;

    public CreateUserDtoRequest getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toRequestUserDto(user);
    }

    public Iterable<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    public CreateUserDtoResponse createUser(CreateUserDtoRequest user) {
        String keycloackUserId =  keycloackRestClient.createKeyCloackUser(
                CreateKeyCloackUserRequestDto.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .build()
        );

        User userDb = User.builder()
                .keycloack_id(keycloackUserId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        log.info("{} user", userDb);
        return userMapper.toCreateUserDto(userRepository.save(userDb));
    }

    @Transactional
    public void deleteUser(String keycloackUserId) {
        keycloackRestClient.deleteKeyCloackUser(keycloackUserId);
        User user = userRepository.findByKeycloack_id(keycloackUserId);
        userRepository.delete(user);

        log.info("{} user delete", keycloackUserId);
    }

    public void assignRoleToUser(AssignRoleToUserRequestDto assignRoleToUserRequestDto) {
        keycloackRestClient.assignRoleToUser(assignRoleToUserRequestDto);
    }

    public void deleteRoleFromUser(AssignRoleToUserRequestDto assignRoleToUserRequestDto) {
        keycloackRestClient.deleteRoleFromUser(assignRoleToUserRequestDto);
    }

    //todo edit usr
}
