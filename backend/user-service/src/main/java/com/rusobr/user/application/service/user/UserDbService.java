package com.rusobr.user.application.service.user;

import com.rusobr.common.enums.UserRole;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.user.UserProfileDetails;
import com.rusobr.user.web.dto.user.UserRoleStrategy;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rusobr.user.web.exception.UserExceptionCode.INVALID_USER_ROLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDbService {

    private final UserService userService;
    private final List<UserRoleStrategy> strategies;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    private Map<UserRole, UserRoleStrategy> roleStrategies;

    //Выполняем паттерн strategy, собираем все реализации обработки всех видов пользователей (parent, student, teacher...)
    //Превращаем в map, где key это роль, а body это details для каждого типа
    @PostConstruct
    public void init() {
        this.roleStrategies = strategies.stream().collect(Collectors.toMap(UserRoleStrategy::getRole, s -> s));
    }

    @Transactional
    public UserResponse create(UserCreateRequest<? extends UserProfileDetails> createUserRequest) {
        //Создаем user в бд
        UserResponse userResponse = userService.create(createUserRequest.user(), createUserRequest.role());
        //Выбираем конкретную реализацию пользователя по роли
        UserRoleStrategy strategy = roleStrategies.get(createUserRequest.role());
        if (strategy == null) {
            throw new ConflictException("Invalid user role", INVALID_USER_ROLE);
        }
        strategy.save(userResponse.id(), createUserRequest.details());

        return userResponse;
    }

    @Transactional
    public UserResponse update(User user, UserUpdateData newUserData, Set<UserRole> newRoles,
                               Map<UserRole, UserProfileDetails> details) {
        if (newUserData.username() != null) user.setUsername(newUserData.username());
        if (newUserData.firstName() != null) user.setFirstName(newUserData.firstName());
        if (newUserData.lastName() != null) user.setLastName(newUserData.lastName());

        Set<UserRole> currentRoles = new HashSet<>(user.getRoles());

        //Удаляем старую роль если она не найдена среди новых DELETE
        currentRoles.stream()
                .filter(currentRole -> !newRoles.contains(currentRole))
                .forEach(role -> roleStrategies.get(role).delete(user.getId()));

        //Добавляем новую роль если она не найдена среди старых SAVE
        newRoles.stream()
                .filter(newRole -> !currentRoles.contains(newRole))
                .forEach(role -> roleStrategies.get(role).save(user.getId(), details.get(role)));

        //Обновляем роль если она найдена среди старых UPDATE
        currentRoles.stream()
                .filter(newRoles::contains)
                .forEach(role -> roleStrategies.get(role).update(user.getId(), details.get(role)));

        user.getRoles().clear();
        user.getRoles().addAll(newRoles);

        return userMapper.toUserResponse(userRepository.save(user));
    }
}
