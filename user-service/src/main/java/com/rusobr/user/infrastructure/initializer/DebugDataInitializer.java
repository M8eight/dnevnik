package com.rusobr.user.infrastructure.initializer;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRoles;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
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
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;


    @Override
    public void run(String... args) throws Exception {
        log.info("=== Запуск инициализации пользователей (Синхронизация ID) ===");

        // 1-5: Ученики (ID: 1, 2, 3, 4, 5)
        createUser("student1", "12345678", "Иван", "Иванов", "student");
        createUser("student2", "12345678", "Мария", "Петрова", "student");
        createUser("student3", "12345678", "Алексей", "Сидоров", "student");
        createUser("student4", "12345678", "Ника", "Демидова", "student");
        createUser("student5", "12345678", "Артем", "Смирнов", "student");

        // 6-9: Родители и Админ (Занимаем промежуточные ID)
        createUser("admin", "12345678", "Иван", "Админов", "admin"); // ID: 6
        createUser("parent1", "12345678", "Елена", "Демидова", "parent"); // ID: 7
        createUser("parent2", "12345678", "Дмитрий", "Смирнов", "parent"); // ID: 8
        createUser("spare_user", "12345678", "Запасной", "Юзер", "admin"); // ID: 9

        // 10-18: Учителя (ID: 10, 11, 12, 13, 14, 15, 16, 17, 18)
        createUser("math_teacher", "12345678", "Петр", "Математиков", "teacher"); // 10
        createUser("lang_teacher", "12345678", "Анна", "Словесник", "teacher");   // 11
        createUser("phys_teacher", "12345678", "Виктор", "Физиков", "teacher");   // 12
        createUser("chem_teacher", "12345678", "Елена", "Химикова", "teacher");   // 13
        createUser("bio_teacher",  "12345678", "Ольга", "Биологова", "teacher");  // 14
        createUser("hist_teacher", "12345678", "Игорь", "Историков", "teacher");  // 15
        createUser("it_teacher",   "12345678", "Семен", "Айтишников", "teacher"); // 16
        createUser("pe_teacher",   "12345678", "Олег", "Спортов", "teacher");     // 17
        createUser("draw_teacher", "12345678", "Борис", "Чертежник", "teacher");  // 18

        log.info("Все тестовые пользователи созданы. Проверьте, что ID в БД совпадают с ожиданиями.");
    }

    private void createUser(String username, String password, String firstName, String lastName, String role) {
        String roleId;
        UserRoles userRole;

        switch (role) {
            case "admin" -> {
                roleId = "fb4ae346-9cbb-4c45-93ad-bd89bfe331a4"; userRole = UserRoles.ADMIN;
            }
            case "teacher" -> {
                roleId = "12535b66-c65b-44fb-a686-49f460f3efba"; userRole = UserRoles.TEACHER;
            }
            case "student" -> {
                roleId = "412fe55a-22e8-494e-98b9-ad47b98abc36"; userRole = UserRoles.STUDENT;
            }
            case "parent" -> {
                roleId = "6696880e-db8e-40a1-9035-eb018ce682e4"; userRole = UserRoles.PARENT;
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }

        String keycloackId = "";
        try {
            keycloackId = keycloackRestClient.createKeyCloackUser(new CreateUserRequest(username, password, firstName, lastName));
        } catch (Exception e) {
            if (e.getMessage().contains("Keycloack User already exists") || e.getMessage().contains("409")) {
                keycloackId = keycloackRestClient.getKeycloackUserByUsername(username).id();
                log.warn("User {} already exists in Keycloak", username);
            } else {
                throw e;
            }
        }

        try {
            keycloackRestClient.assignRoleToUser(new AssignRoleToUserRequest(keycloackId, userRole, roleId));
        } catch (Exception ignored) {}

        User user = userRepository.save(User.builder()
                .username(username)
                .keycloackId(keycloackId)
                .firstName(firstName)
                .lastName(lastName)
                .roles(Collections.singleton(userRole))
                .build();
        userRepository.save(user);

        switch (role) {
            case "teacher" -> {
                teacherRepository.save(Teacher.builder().user(user).build());
            }
            case "student" -> {
                studentRepository.save(Student.builder().user(user).build());
            }
            case "parent" -> {
                parentRepository.save(Parent.builder().user(user).build());
            }
        }

        switch (role) {
            case "teacher" -> teacherRepository.save(Teacher.builder().user(user).build());
            case "student" -> studentRepository.save(Student.builder().user(user).build());
            case "parent"  -> parentRepository.save(Parent.builder().user(user).build());
        }

        log.info("Создан {} с ID: {}", username, user.getId());
    }
}