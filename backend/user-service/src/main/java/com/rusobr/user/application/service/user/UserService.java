package com.rusobr.user.application.service.user;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.webClient.KeycloakRestClient;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.specification.UserSpecification;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.rusobr.user.web.exception.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakRestClient keycloakRestClient;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toUserResponse(getByIdInternal(id));
    }

    @Transactional(readOnly = true)
    public User getByIdInternal(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User by id: %d not found".formatted(id), USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username, Long id) {
        return userRepository.existsByUsernameAndIdNot(username, id);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllByFilter(Pageable pageable, UserRole role, String fullNameSearch) {
        Specification<User> specification = UserSpecification.findByRole(role).and(UserSpecification.findByFullNameFuzzy(fullNameSearch));
        return userRepository.findAll(specification, pageable).map(userMapper::toUserResponse);
    }

    @Transactional
    public UserResponse create(UserDataDto reqDto, UserRole role) {
            User user = userMapper.toUser(reqDto, Collections.singleton(role));
            return userMapper.toCreateUserResponse(userRepository.save(user));
    }

    @Transactional
    public void setKeycloakId(String kId, Long userId) {
        userRepository.setKeycloakId(kId, userId);
    }

    public List<KeycloakRole> getAllRoles() {
        return keycloakRestClient.getAllKeycloakRoles();
    }

    @Transactional
    public void deleteUserCascade(Long id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User by id: %d not found".formatted(id), USER_NOT_FOUND));
        userRepository.delete(user);

        applicationEventPublisher.publishEvent(new UserDeletedEvent(user.getId(), user.getRoles()));
    }

}
