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
import com.rusobr.user.infrastructure.webClient.KeycloakRestClient;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
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
    private final KeycloakRestClient keycloakRestClient;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public void run(String... args) {
        log.info("=== Запуск инициализации пользователей ===");

        // ── Администрация ─────────────────────────────────────────────────────
        createUser("director",         "12345678", "Елена",       "Директорова",                    "admin");
        createUser("deputy_academic",  "12345678", "Валентина",   "Завучева",                       "admin");

        // ── Учителя (20 чел.) ─────────────────────────────────────────────────
        // Математика / физика / информатика
        createUser("teacher_math1",    "12345678", "Ирина",       "Николаевна (Алгебра)",           "teacher");
        createUser("teacher_math2",    "12345678", "Геннадий",    "Борисович (Алгебра)",            "teacher");
        createUser("teacher_geom",     "12345678", "Светлана",    "Юрьевна (Геометрия)",            "teacher");
        createUser("teacher_phys1",    "12345678", "Виктор",      "Степанович (Физика)",            "teacher");
        createUser("teacher_phys2",    "12345678", "Людмила",     "Фёдоровна (Физика)",             "teacher");
        createUser("teacher_it1",      "12345678", "Александр",   "Сергеевич (Информатика)",        "teacher");
        createUser("teacher_it2",      "12345678", "Роман",       "Игоревич (Информатика)",         "teacher");
        // Гуманитарные
        createUser("teacher_rus1",     "12345678", "Оксана",      "Дмитриевна (Русский)",           "teacher");
        createUser("teacher_rus2",     "12345678", "Надежда",     "Петровна (Русский)",             "teacher");
        createUser("teacher_lit",      "12345678", "Марина",      "Олеговна (Литература)",          "teacher");
        createUser("teacher_hist1",    "12345678", "Игорь",       "Анатольевич (История)",          "teacher");
        createUser("teacher_hist2",    "12345678", "Зинаида",     "Константиновна (История)",       "teacher");
        createUser("teacher_soc",      "12345678", "Артём",       "Владиленович (Обществознание)",  "teacher");
        // Иностранные языки
        createUser("teacher_eng1",     "12345678", "Анна",        "Павловна (Английский)",          "teacher");
        createUser("teacher_eng2",     "12345678", "Дарья",       "Александровна (Английский)",     "teacher");
        createUser("teacher_de",       "12345678", "Ульрике",     "Вернер (Немецкий)",              "teacher");
        // Естественные науки
        createUser("teacher_chem",     "12345678", "Татьяна",     "Васильевна (Химия)",             "teacher");
        createUser("teacher_bio",      "12345678", "Евгения",     "Романовна (Биология)",           "teacher");
        createUser("teacher_geo",      "12345678", "Николай",     "Львович (География)",            "teacher");
        // Прочее
        createUser("teacher_pe1",      "12345678", "Евгений",     "Владимирович (Физра)",           "teacher");
        createUser("teacher_pe2",      "12345678", "Карина",      "Эдуардовна (Физра)",             "teacher");
        createUser("teacher_art",      "12345678", "Полина",      "Юрьевна (Изо)",                  "teacher");
        createUser("teacher_music",    "12345678", "Борис",       "Семёнович (Музыка)",             "teacher");
        createUser("teacher_tech",     "12345678", "Михаил",      "Захарович (Технология)",         "teacher");

        // ── 8А класс (28 учеников) ────────────────────────────────────────────
        createUser("ivanov_i",         "12345678", "Иван",        "Иванов",        "student");
        createUser("petrova_m",        "12345678", "Мария",       "Петрова",       "student");
        createUser("sidorov_a",        "12345678", "Алексей",     "Сидоров",       "student");
        createUser("demidova_n",       "12345678", "Ника",        "Демидова",      "student");
        createUser("smirnov_a",        "12345678", "Артём",       "Смирнов",       "student");
        createUser("kuznetsova_e",     "12345678", "Екатерина",   "Кузнецова",     "student");
        createUser("popov_d",          "12345678", "Дмитрий",     "Попов",         "student");
        createUser("sokolova_a",       "12345678", "Анастасия",   "Соколова",      "student");
        createUser("lebedev_m",        "12345678", "Максим",      "Лебедев",       "student");
        createUser("kozlova_p",        "12345678", "Полина",      "Козлова",       "student");
        createUser("novikov_i",        "12345678", "Илья",        "Новиков",       "student");
        createUser("morozova_v",       "12345678", "Виктория",    "Морозова",      "student");
        createUser("volkov_r",         "12345678", "Роман",       "Волков",        "student");
        createUser("zaitseva_k",        "12345678", "Ксения",      "Зайцева",       "student");
        createUser("sorokin_t",        "12345678", "Тимур",       "Сорокин",       "student");
        createUser("pavlova_d",        "12345678", "Диана",       "Павлова",       "student");
        createUser("fedorov_n",        "12345678", "Никита",      "Фёдоров",       "student");
        createUser("alexandrova_s",    "12345678", "София",       "Александрова",  "student");
        createUser("morozov_eg",       "12345678", "Егор",        "Морозов",       "student");
        createUser("nikitin_y",        "12345678", "Юрий",        "Никитин",       "student");
        createUser("titova_l",         "12345678", "Лилия",       "Титова",        "student");
        createUser("semyonov_v",       "12345678", "Влад",        "Семёнов",       "student");
        createUser("egorova_al",       "12345678", "Алина",       "Егорова",       "student");
        createUser("kovalev_st",       "12345678", "Степан",      "Ковалёв",       "student");
        createUser("zakharova_an",     "12345678", "Анна",        "Захарова",      "student");
        createUser("belov_ki",         "12345678", "Кирилл",      "Белов",         "student");
        createUser("orlova_yu",        "12345678", "Юлия",        "Орлова",        "student");
        createUser("vorobyev_ar",      "12345678", "Арсений",     "Воробьёв",      "student");

        // ── 8Б класс (27 учеников) ────────────────────────────────────────────
        createUser("8b_abramov_fg",    "12345678", "Фёдор",       "Абрамов",       "student");
        createUser("8b_baranova_el",   "12345678", "Елена",       "Баранова",      "student");
        createUser("8b_gavrilov_pl",   "12345678", "Павел",       "Гаврилов",      "student");
        createUser("8b_danilova_ir",   "12345678", "Ирина",       "Данилова",      "student");
        createUser("8b_eremenko_mi",   "12345678", "Михаил",      "Ерёменко",      "student");
        createUser("8b_zhukova_nt",     "12345678", "Наталья",     "Жукова",        "student");
        createUser("8b_zimin_st",      "12345678", "Степан",      "Зимин",         "student");
        createUser("8b_ignatova_al",   "12345678", "Алина",       "Игнатова",      "student");
        createUser("8b_kabanova_ks",   "12345678", "Ксения",      "Кабанова",      "student");
        createUser("8b_lavrov_dm",     "12345678", "Дмитрий",     "Лавров",        "student");
        createUser("8b_muravyeva_ok",  "12345678", "Ольга",       "Муравьёва",     "student");
        createUser("8b_nesterov_vl",   "12345678", "Владислав",   "Нестеров",      "student");
        createUser("8b_odintsova_ma",  "12345678", "Марина",      "Одинцова",      "student");
        createUser("8b_petrov_gb",     "12345678", "Глеб",        "Петров",        "student");
        createUser("8b_rybakova_ek",   "12345678", "Екатерина",   "Рыбакова",      "student");
        createUser("8b_savelyev_ar",   "12345678", "Артём",       "Савельев",      "student");
        createUser("8b_tarasova_vi",   "12345678", "Виктория",    "Тарасова",      "student");
        createUser("8b_ulyanova_ag",   "12345678", "Аглая",       "Ульянова",      "student");
        createUser("8b_filatov_eg",    "12345678", "Егор",        "Филатов",       "student");
        createUser("8b_kharitonov_il", "12345678", "Илья",        "Харитонов",     "student");
        createUser("8b_tsvetaeva_an",  "12345678", "Анна",        "Цветаева",      "student");
        createUser("8b_chernov_ki",    "12345678", "Кирилл",      "Чернов",        "student");
        createUser("8b_shilova_po",    "12345678", "Полина",      "Шилова",        "student");
        createUser("8b_shcherbakov_n", "12345678", "Никита",      "Щербаков",      "student");
        createUser("8b_ershov_ma",     "12345678", "Максим",      "Ершов",         "student");
        createUser("8b_yakovleva_da",  "12345678", "Дарья",       "Яковлева",      "student");
        createUser("8b_yudin_ro",      "12345678", "Роман",       "Юдин",          "student");

        // ── 9А класс (26 учеников) ────────────────────────────────────────────
        createUser("9a_astakhov_bo",   "12345678", "Борис",       "Астахов",       "student");
        createUser("9a_bogdanova_el",  "12345678", "Елена",       "Богданова",     "student");
        createUser("9a_voronin_se",    "12345678", "Сергей",      "Воронин",       "student");
        createUser("9a_guseva_ta",     "12345678", "Тамара",      "Гусева",        "student");
        createUser("9a_dyachenko_ni",  "12345678", "Николай",     "Дьяченко",      "student");
        createUser("9a_efimov_pa",     "12345678", "Павел",       "Ефимов",        "student");
        createUser("9a_zhukova_an",    "12345678", "Анна",        "Жукова",        "student");
        createUser("9a_zhuravlev_gr",  "12345678", "Григорий",    "Журавлёв",      "student");
        createUser("9a_ivanova_so",    "12345678", "Соня",        "Иванова",       "student");
        createUser("9a_komissarov_va", "12345678", "Валентин",    "Комиссаров",    "student");
        createUser("9a_loginova_ma",   "12345678", "Мария",       "Логинова",      "student");
        createUser("9a_makarov_vi",    "12345678", "Виктор",      "Макаров",       "student");
        createUser("9a_nazarova_ag",   "12345678", "Агата",       "Назарова",      "student");
        createUser("9a_osipov_fe",     "12345678", "Фёдор",       "Осипов",        "student");
        createUser("9a_potapova_al",   "12345678", "Алиса",       "Потапова",      "student");
        createUser("9a_rozanov_mi",    "12345678", "Михаил",      "Розанов",       "student");
        createUser("9a_stepanova_yu",  "12345678", "Юлия",        "Степанова",     "student");
        createUser("9a_titov_da",      "12345678", "Даниил",      "Титов",         "student");
        createUser("9a_usov_ri",       "12345678", "Ринат",       "Усов",          "student");
        createUser("9a_filatova_el",   "12345678", "Елизавета",   "Филатова",      "student");
        createUser("9a_cherepanov_il", "12345678", "Илья",        "Черепанов",     "student");
        createUser("9a_shmeleva_ok",   "12345678", "Оксана",      "Шмелёва",       "student");
        createUser("9a_eliseev_ar",    "12345678", "Арсений",     "Елисеев",       "student");
        createUser("9a_yartseva_ka",   "12345678", "Карина",      "Ярцева",        "student");
        createUser("9a_abrahamyan_ar", "12345678", "Арман",       "Абрамян",       "student");
        createUser("9a_belousov_eg",   "12345678", "Егор",        "Белоусов",      "student");

        // ── Родители ──────────────────────────────────────────────────────────
        // Родители 8А
        createUser("parent_ivanova",    "12345678", "Наталья",    "Иванова",       "parent");
        createUser("parent_petrov",     "12345678", "Сергей",     "Петров",        "parent");
        createUser("parent_sidorova",   "12345678", "Ольга",      "Сидорова",      "parent");
        createUser("parent_demidov",    "12345678", "Андрей",     "Демидов",       "parent");
        createUser("parent_smirnova",   "12345678", "Марина",     "Смирнова",      "parent");
        createUser("parent_kuznetsov",  "12345678", "Павел",      "Кузнецов",      "parent");
        createUser("parent_popova",     "12345678", "Светлана",   "Попова",        "parent");
        createUser("parent_sokolov",    "12345678", "Дмитрий",    "Соколов",       "parent");
        createUser("parent_lebedeva",   "12345678", "Елена",      "Лебедева",      "parent");
        createUser("parent_kozlov",     "12345678", "Виктор",     "Козлов",        "parent");
        createUser("parent_novikova",   "12345678", "Ирина",      "Новикова",      "parent");
        createUser("parent_morozov",    "12345678", "Игорь",      "Морозов",       "parent");
        createUser("parent_volkova",    "12345678", "Татьяна",    "Волкова",       "parent");
        createUser("parent_zaitsev",    "12345678", "Роман",      "Зайцев",        "parent");
        // Родители 8Б
        createUser("parent_abramova",   "12345678", "Людмила",    "Абрамова",      "parent");
        createUser("parent_gavrilov",   "12345678", "Николай",    "Гаврилов",      "parent");
        createUser("parent_eremenko",   "12345678", "Алексей",    "Ерёменко",      "parent");
        createUser("parent_zimina",     "12345678", "Валентина",  "Зимина",        "parent");
        createUser("parent_lavrov",     "12345678", "Дмитрий",    "Лавров",        "parent");
        createUser("parent_nesterova",  "12345678", "Жанна",      "Нестерова",     "parent");
        createUser("parent_petrov_8b",  "12345678", "Геннадий",   "Петров",        "parent");
        createUser("parent_savelyeva",  "12345678", "Анжелика",   "Савельева",     "parent");
        createUser("parent_filatov",    "12345678", "Евгений",    "Филатов",       "parent");
        // Родители 9А
        createUser("parent_astakhova",  "12345678", "Любовь",     "Астахова",      "parent");
        createUser("parent_voronin",    "12345678", "Сергей",     "Воронин",       "parent");
        createUser("parent_gusev",      "12345678", "Анатолий",   "Гусев",         "parent");
        createUser("parent_efimova",    "12345678", "Галина",     "Ефимова",       "parent");
        createUser("parent_komissarov", "12345678", "Геннадий",   "Комиссаров",    "parent");
        createUser("parent_makarova",   "12345678", "Наталья",    "Макарова",      "parent");
        createUser("parent_rozanova",   "12345678", "Людмила",    "Розанова",      "parent");
        createUser("parent_stepanov",   "12345678", "Владимир",   "Степанов",      "parent");

        log.info("=== Инициализация завершена ===");
        log.info("=== ID пользователей (для academic-service) ===");
        userRepository.findAll().forEach(u ->
                log.info("username={} id={} roles={}", u.getUsername(), u.getId(), u.getRoles())
        );
    }

    private void createUser(String username, String password, String firstName, String lastName, String role) {
        if (userRepository.existsByUsername(username)) {
            User existing = userRepository.findByUsername(username).orElseThrow();
            log.info("User {} already exists in DB with id={}, skipping.", username, existing.getId());
            return;
        }

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

        String keycloakId;
        try {
            keycloakId = keycloakRestClient.createKeyCloakUser(
                    new CreateUserRequest(username, password, firstName, lastName));
        } catch (Exception e) {
            if (e.getMessage().contains("Keycloak User already exists") || e.getMessage().contains("409")) {
                keycloakId = keycloakRestClient.getKeycloakUserByUsername(username).id();
                log.warn("User {} already exists in Keycloak, fetched id={}", username, keycloakId);
            } else {
                throw e;
            }
        }

        try {
            keycloakRestClient.assignRoleToUser(new AssignRoleToUserRequest(keycloakId, userRole, roleId));
        } catch (Exception ignored) {}

        User user = userRepository.save(User.builder()
                .username(username)
                .keycloakId(keycloakId)
                .firstName(firstName)
                .lastName(lastName)
                .roles(new HashSet<>(Set.of(userRole)))
                .build());

        log.info("Created user: username={} id={} role={}", username, user.getId(), role);

        switch (role) {
            case "teacher" -> teacherRepository.save(
                    Teacher.builder().phoneNumber("+7-900-000-0000").email(username + "@school.ru").user(user).build());
            case "student" -> studentRepository.save(
                    Student.builder().studyProfile("Социо-эконом").user(user).build());
            case "parent"  -> parentRepository.save(
                    Parent.builder().user(user).build());
        }
    }
}