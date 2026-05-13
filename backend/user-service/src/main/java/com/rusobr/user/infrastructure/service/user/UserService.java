package com.rusobr.user.infrastructure.service.user;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.specification.UserSpecification;
import com.rusobr.user.infrastructure.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.role.KeycloakRole;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakRestClient keycloakRestClient;
    private final UserMapper userMapper;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAllByFilter(Pageable pageable, UserRole role, String fullNameSearch) {
        Specification<User> specification = UserSpecification.findByRole(role).and(UserSpecification.findByFullNameFuzzy(fullNameSearch));
        return userRepository.findAll(specification, pageable).map(userMapper::toUserResponse);
    }

    public UserResponse createUser(UserDataDto reqDto, String keycloakUserId, UserRole role) {
            User user = userMapper.toUser(reqDto, keycloakUserId, Collections.singleton(role));
            return userMapper.toCreateUserResponse(userRepository.save(user));
    }

    @Transactional
    public List<KeycloakRole> getAllRoles() {
        return keycloakRestClient.getAllKeycloakRoles();
    }

    @Transactional
    public void deleteUserCascade(Long id) {
        User user = userRepository.findUserFetchById(id);
        switch (user.getRoles().iterator().next()) {
            case PARENT -> parentRepository.deleteById(user.getId());
            case STUDENT -> studentRepository.deleteById(user.getId());
            case TEACHER -> teacherRepository.deleteById(user.getId());
        }
        userRepository.delete(user);
    }

}
