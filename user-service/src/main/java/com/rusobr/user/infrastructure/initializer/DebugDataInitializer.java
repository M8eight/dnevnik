package com.rusobr.user.infrastructure.initializer;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRoles;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.webClient.KeycloackRestClient;
import com.rusobr.user.web.dto.keycloack.CreateUserRequest;
import com.rusobr.user.web.dto.keycloack.role.AssignRoleToUserRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
@Deprecated
public class DebugDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final KeycloackRestClient keycloackRestClient;

    @Override
    public void run(String... args) throws Exception {
        createUser("Admin", "12345678", "Иван", "Админов", "admin");

        // Учителя
        createUser("Teacher1", "12345678", "Мария", "Иванова", "teacher");
        createUser("Teacher2", "12345678", "Сергей", "Петров", "teacher");

        // Ученики
        createUser("Student1", "12345678", "Ника", "Демидова", "student");
        createUser("Student2", "12345678", "Артем", "Смирнов", "student");

        // Родители
        createUser("Parent1", "12345678", "Елена", "Демидова", "parent");
        createUser("Parent2", "12345678", "Дмитрий", "Смирнов", "parent");

        log.info("Все тестовые пользователи созданы.");
    }

    private void createUser(String username, String password, String firstName, String lastName, String role) {
        String roleId;
        UserRoles userRole;

        // Определяем параметры роли
        switch (role) {
            case "admin" -> { roleId = "fb4ae346-9cbb-4c45-93ad-bd89bfe331a4"; userRole = UserRoles.ADMIN; }
            case "teacher" -> { roleId = "12535b66-c65b-44fb-a686-49f460f3efba"; userRole = UserRoles.TEACHER; }
            case "student" -> { roleId = "412fe55a-22e8-494e-98b9-ad47b98abc36"; userRole = UserRoles.STUDENT; }
            case "parent" -> { roleId = "6696880e-db8e-40a1-9035-eb018ce682e4"; userRole = UserRoles.PARENT; }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }

        String keycloackId = "";
        try {
            keycloackId = keycloackRestClient.createKeyCloackUser(new CreateUserRequest(username, password, firstName, lastName));
        } catch (Exception e) {
            if (e.getMessage().contains("Keycloack User already exists")) {
                keycloackId = keycloackRestClient.getKeycloackUserByUsername(username).id();
                log.warn("User {} already exists in Keycloak", username);
            } else  {
                throw e;
            }
        }

        // Выполняем действия один раз после определения параметров
        try {
            keycloackRestClient.assignRoleToUser(new AssignRoleToUserRequest(keycloackId, userRole, roleId));
        } catch (Exception ignored) {}

        userRepository.save(User.builder()
                .username(username)
                .keycloackId(keycloackId)
                .firstName(firstName)
                .lastName(lastName)
                .roles(Collections.singleton(userRole))
                .build());

        log.info("User {} created with role {}", username, role);
    }
}
