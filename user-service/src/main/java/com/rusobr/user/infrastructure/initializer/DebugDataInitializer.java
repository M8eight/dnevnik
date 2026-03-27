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

import java.util.HashSet;
import java.util.Set;

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

    @Override
    public void run(String... args) {
        log.info("=== Запуск инициализации пользователей (Синхронизация ID) ===");

        // 1-12: Ученики 8 "А" (ID: 1-12)
        createUser("ivanov_i", "12345678", "Иван", "Иванов", "student");
        createUser("petrova_m", "12345678", "Мария", "Петрова", "student");
        createUser("sidorov_a", "12345678", "Алексей", "Сидоров", "student");
        createUser("demidova_n", "12345678", "Ника", "Демидова", "student");
        createUser("smirnov_a", "12345678", "Артем", "Смирнов", "student");
        createUser("kuznetsova_e", "12345678", "Екатерина", "Кузнецова", "student");
        createUser("popov_d", "12345678", "Дмитрий", "Попов", "student");
        createUser("sokolova_a", "12345678", "Анастасия", "Соколова", "student");
        createUser("lebedev_m", "12345678", "Максим", "Лебедев", "student");
        createUser("kozlova_p", "12345678", "Полина", "Козлова", "student");
        createUser("novikov_i", "12345678", "Илья", "Новиков", "student");
        createUser("morozova_v", "12345678", "Виктория", "Морозова", "student");

        // 13-16: Администрация и Родители (ID: 13-16)
        createUser("admin", "12345678", "Елена", "Директорова", "admin");
        createUser("parent_demidova", "12345678", "Ольга", "Демидова", "parent");
        createUser("parent_smirnov", "12345678", "Сергей", "Смирнов", "parent");
        createUser("parent_ivanova", "12345678", "Наталья", "Иванова", "parent");

        // 17-26: Учителя предметники (ID: 17-26)
        createUser("teacher_math", "12345678", "Ирина", "Николаевна (Математика)", "teacher");
        createUser("teacher_rus", "12345678", "Светлана", "Юрьевна (Русский)", "teacher");
        createUser("teacher_phys", "12345678", "Виктор", "Степанович (Физика)", "teacher");
        createUser("teacher_chem", "12345678", "Татьяна", "Васильевна (Химия)", "teacher");
        createUser("teacher_bio", "12345678", "Марина", "Олеговна (Биология)", "teacher");
        createUser("teacher_hist", "12345678", "Игорь", "Анатольевич (История)", "teacher");
        createUser("teacher_it", "12345678", "Александр", "Сергеевич (Информатика)", "teacher");
        createUser("teacher_pe", "12345678", "Евгений", "Владимирович (Физра)", "teacher");
        createUser("teacher_eng", "12345678", "Анна", "Павловна (Английский)", "teacher");
        createUser("teacher_lit", "12345678", "Оксана", "Дмитриевна (Литература)", "teacher");

        log.info("=== Тестовые пользователи созданы. ID учителей начинаются с 17. ===");
    }

    private void createUser(String username, String password, String firstName, String lastName, String role) {
        String roleId;
        UserRoles userRole;

        switch (role) {
            case "admin" -> {
                roleId = "fb4ae346-9cbb-4c45-93ad-bd89bfe331a4";
                userRole = UserRoles.ADMIN;
            }
            case "teacher" -> {
                roleId = "12535b66-c65b-44fb-a686-49f460f3efba";
                userRole = UserRoles.TEACHER;
            }
            case "student" -> {
                roleId = "412fe55a-22e8-494e-98b9-ad47b98abc36";
                userRole = UserRoles.STUDENT;
            }
            case "parent" -> {
                roleId = "6696880e-db8e-40a1-9035-eb018ce682e4";
                userRole = UserRoles.PARENT;
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }

        String keycloackId;
        try {
            keycloackId = keycloackRestClient.createKeyCloackUser(
                    new CreateUserRequest(username, password, firstName, lastName));
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
                .roles(new HashSet<>(Set.of(userRole)))
                .build());

        switch (role) {
            case "teacher" -> teacherRepository.save(Teacher.builder().user(user).build());
            case "student" -> studentRepository.save(Student.builder().user(user).build());
            case "parent"  -> parentRepository.save(Parent.builder().user(user).build());
        }
    }
}