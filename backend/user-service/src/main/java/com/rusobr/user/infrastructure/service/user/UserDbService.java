package com.rusobr.user.infrastructure.service.user;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.service.user.strategy.CreateUserStrategy;
import com.rusobr.user.web.dto.user.UserRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDbService {

    private final UserService userService;
    private final List<CreateUserStrategy> saversList;

    private Map<UserRole, CreateUserStrategy> savers;

    //Выполняем паттерн strategy, собираем все реализации обработки всех видов пользователей (parent, student, teacher...)
    //Превращаем в map, где key это роль, а body это details для каждого типа
    @PostConstruct
    public void init() {
        this.savers = saversList.stream().collect(Collectors.toMap(CreateUserStrategy::getRole, s -> s));
    }

    @Transactional
    public UserResponse createUserDb(UserRequest<? extends UserProfileDetails> createUserRequest, String keycloakId) {
        //Создаем user в бд
        UserResponse userResponse = userService.createUser(createUserRequest.user(), keycloakId);
        //Выбираем конкретную реализацию пользователя по роли
        CreateUserStrategy strategy = savers.get(createUserRequest.role());
        if (strategy == null) {
            throw new ConflictException("Invalid user role");
        }
        strategy.save(userResponse.id(), createUserRequest.details());

        return userResponse;
    }
}
