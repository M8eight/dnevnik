package com.rusobr.user.infrastructure.initializer;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.webClient.KeycloakRestClient;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.keycloak.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.user.UserDataDto;
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

        // ── Администрация (ID 1–2) ────────────────────────────────────────────
        createUser("director",         "12345678", "Елена",       "Директорова",             "admin");
        createUser("deputy_academic",  "12345678", "Валентина",   "Завучева",                "admin");

        // ── Учителя (ID 3–26, итого 24 учителя) ──────────────────────────────

        // Математика и геометрия
        createUser("teacher_math1",    "12345678", "Ирина",       "Николаевна",              "teacher"); // 3
        createUser("teacher_math2",    "12345678", "Геннадий",    "Борисович",               "teacher"); // 4
        createUser("teacher_geom",     "12345678", "Светлана",    "Юрьевна",                 "teacher"); // 5

        // Физика
        createUser("teacher_phys1",    "12345678", "Виктор",      "Степанович",              "teacher"); // 6
        createUser("teacher_phys2",    "12345678", "Людмила",     "Фёдоровна",               "teacher"); // 7

        // Информатика
        createUser("teacher_it1",      "12345678", "Александр",   "Сергеевич",               "teacher"); // 8
        createUser("teacher_it2",      "12345678", "Роман",       "Игоревич",                "teacher"); // 9

        // Гуманитарные
        createUser("teacher_rus1",     "12345678", "Оксана",      "Дмитриевна",              "teacher"); // 10
        createUser("teacher_rus2",     "12345678", "Надежда",     "Петровна",                "teacher"); // 11
        createUser("teacher_lit",      "12345678", "Марина",      "Олеговна",                "teacher"); // 12
        createUser("teacher_hist1",    "12345678", "Игорь",       "Анатольевич",             "teacher"); // 13
        createUser("teacher_hist2",    "12345678", "Зинаида",     "Константиновна",          "teacher"); // 14
        createUser("teacher_soc",      "12345678", "Артём",       "Владиленович",            "teacher"); // 15

        // Иностранные языки
        createUser("teacher_eng1",     "12345678", "Анна",        "Павловна",                "teacher"); // 16
        createUser("teacher_eng2",     "12345678", "Дарья",       "Александровна",           "teacher"); // 17
        createUser("teacher_de",       "12345678", "Ульрике",     "Вернер",                  "teacher"); // 18

        // Естественные науки
        createUser("teacher_chem",     "12345678", "Татьяна",     "Васильевна",              "teacher"); // 19
        createUser("teacher_bio",      "12345678", "Евгения",     "Романовна",               "teacher"); // 20
        createUser("teacher_geo",      "12345678", "Николай",     "Львович",                 "teacher"); // 21

        // Прочее
        createUser("teacher_pe1",      "12345678", "Евгений",     "Владимирович",            "teacher"); // 22
        createUser("teacher_pe2",      "12345678", "Карина",      "Эдуардовна",              "teacher"); // 23
        createUser("teacher_art",      "12345678", "Полина",      "Юрьевна",                 "teacher"); // 24
        createUser("teacher_music",    "12345678", "Борис",       "Семёнович",               "teacher"); // 25
        createUser("teacher_tech",     "12345678", "Михаил",      "Захарович",               "teacher"); // 26

        // ── 8А класс (ID 27–54, 28 учеников) ─────────────────────────────────
        createUser("ivanov_i",         "12345678", "Иван",        "Иванов",        "student"); // 27
        createUser("petrova_m",        "12345678", "Мария",       "Петрова",       "student"); // 28
        createUser("sidorov_a",        "12345678", "Алексей",     "Сидоров",       "student"); // 29
        createUser("demidova_n",       "12345678", "Ника",        "Демидова",      "student"); // 30
        createUser("smirnov_a",        "12345678", "Артём",       "Смирнов",       "student"); // 31
        createUser("kuznetsova_e",     "12345678", "Екатерина",   "Кузнецова",     "student"); // 32
        createUser("popov_d",          "12345678", "Дмитрий",     "Попов",         "student"); // 33
        createUser("sokolova_a",       "12345678", "Анастасия",   "Соколова",      "student"); // 34
        createUser("lebedev_m",        "12345678", "Максим",      "Лебедев",       "student"); // 35
        createUser("kozlova_p",        "12345678", "Полина",      "Козлова",       "student"); // 36
        createUser("novikov_i",        "12345678", "Илья",        "Новиков",       "student"); // 37
        createUser("morozova_v",       "12345678", "Виктория",    "Морозова",      "student"); // 38
        createUser("volkov_r",         "12345678", "Роман",       "Волков",        "student"); // 39
        createUser("zaitseva_k",       "12345678", "Ксения",      "Зайцева",       "student"); // 40
        createUser("sorokin_t",        "12345678", "Тимур",       "Сорокин",       "student"); // 41
        createUser("pavlova_d",        "12345678", "Диана",       "Павлова",       "student"); // 42
        createUser("fedorov_n",        "12345678", "Никита",      "Фёдоров",       "student"); // 43
        createUser("alexandrova_s",    "12345678", "София",       "Александрова",  "student"); // 44
        createUser("morozov_eg",       "12345678", "Егор",        "Морозов",       "student"); // 45
        createUser("nikitin_y",        "12345678", "Юрий",        "Никитин",       "student"); // 46
        createUser("titova_l",         "12345678", "Лилия",       "Титова",        "student"); // 47
        createUser("semyonov_v",       "12345678", "Влад",        "Семёнов",       "student"); // 48
        createUser("egorova_al",       "12345678", "Алина",       "Егорова",       "student"); // 49
        createUser("kovalev_st",       "12345678", "Степан",      "Ковалёв",       "student"); // 50
        createUser("zakharova_an",     "12345678", "Анна",        "Захарова",      "student"); // 51
        createUser("belov_ki",         "12345678", "Кирилл",      "Белов",         "student"); // 52
        createUser("orlova_yu",        "12345678", "Юлия",        "Орлова",        "student"); // 53
        createUser("vorobyev_ar",      "12345678", "Арсений",     "Воробьёв",      "student"); // 54

        // ── 8Б класс (ID 55–81, 27 учеников) ─────────────────────────────────
        createUser("8b_abramov_fg",    "12345678", "Фёдор",       "Абрамов",       "student"); // 55
        createUser("8b_baranova_el",   "12345678", "Елена",       "Баранова",      "student"); // 56
        createUser("8b_gavrilov_pl",   "12345678", "Павел",       "Гаврилов",      "student"); // 57
        createUser("8b_danilova_ir",   "12345678", "Ирина",       "Данилова",      "student"); // 58
        createUser("8b_eremenko_mi",   "12345678", "Михаил",      "Ерёменко",      "student"); // 59
        createUser("8b_zhukova_nt",    "12345678", "Наталья",     "Жукова",        "student"); // 60
        createUser("8b_zimin_st",      "12345678", "Степан",      "Зимин",         "student"); // 61
        createUser("8b_ignatova_al",   "12345678", "Алина",       "Игнатова",      "student"); // 62
        createUser("8b_kabanova_ks",   "12345678", "Ксения",      "Кабанова",      "student"); // 63
        createUser("8b_lavrov_dm",     "12345678", "Дмитрий",     "Лавров",        "student"); // 64
        createUser("8b_muravyeva_ok",  "12345678", "Ольга",       "Муравьёва",     "student"); // 65
        createUser("8b_nesterov_vl",   "12345678", "Владислав",   "Нестеров",      "student"); // 66
        createUser("8b_odintsova_ma",  "12345678", "Марина",      "Одинцова",      "student"); // 67
        createUser("8b_petrov_gb",     "12345678", "Глеб",        "Петров",        "student"); // 68
        createUser("8b_rybakova_ek",   "12345678", "Екатерина",   "Рыбакова",      "student"); // 69
        createUser("8b_savelyev_ar",   "12345678", "Артём",       "Савельев",      "student"); // 70
        createUser("8b_tarasova_vi",   "12345678", "Виктория",    "Тарасова",      "student"); // 71
        createUser("8b_ulyanova_ag",   "12345678", "Аглая",       "Ульянова",      "student"); // 72
        createUser("8b_filatov_eg",    "12345678", "Егор",        "Филатов",       "student"); // 73
        createUser("8b_kharitonov_il", "12345678", "Илья",        "Харитонов",     "student"); // 74
        createUser("8b_tsvetaeva_an",  "12345678", "Анна",        "Цветаева",      "student"); // 75
        createUser("8b_chernov_ki",    "12345678", "Кирилл",      "Чернов",        "student"); // 76
        createUser("8b_shilova_po",    "12345678", "Полина",      "Шилова",        "student"); // 77
        createUser("8b_shcherbakov_n", "12345678", "Никита",      "Щербаков",      "student"); // 78
        createUser("8b_ershov_ma",     "12345678", "Максим",      "Ершов",         "student"); // 79
        createUser("8b_yakovleva_da",  "12345678", "Дарья",       "Яковлева",      "student"); // 80
        createUser("8b_yudin_ro",      "12345678", "Роман",       "Юдин",          "student"); // 81

        // ── 9А класс (ID 82–107, 26 учеников) ────────────────────────────────
        createUser("9a_astakhov_bo",   "12345678", "Борис",       "Астахов",       "student"); // 82
        createUser("9a_bogdanova_el",  "12345678", "Елена",       "Богданова",     "student"); // 83
        createUser("9a_voronin_se",    "12345678", "Сергей",      "Воронин",       "student"); // 84
        createUser("9a_guseva_ta",     "12345678", "Тамара",      "Гусева",        "student"); // 85
        createUser("9a_dyachenko_ni",  "12345678", "Николай",     "Дьяченко",      "student"); // 86
        createUser("9a_efimov_pa",     "12345678", "Павел",       "Ефимов",        "student"); // 87
        createUser("9a_zhukova_an",    "12345678", "Анна",        "Жукова",        "student"); // 88
        createUser("9a_zhuravlev_gr",  "12345678", "Григорий",    "Журавлёв",      "student"); // 89
        createUser("9a_ivanova_so",    "12345678", "Соня",        "Иванова",       "student"); // 90
        createUser("9a_komissarov_va", "12345678", "Валентин",    "Комиссаров",    "student"); // 91
        createUser("9a_loginova_ma",   "12345678", "Мария",       "Логинова",      "student"); // 92
        createUser("9a_makarov_vi",    "12345678", "Виктор",      "Макаров",       "student"); // 93
        createUser("9a_nazarova_ag",   "12345678", "Агата",       "Назарова",      "student"); // 94
        createUser("9a_osipov_fe",     "12345678", "Фёдор",       "Осипов",        "student"); // 95
        createUser("9a_potapova_al",   "12345678", "Алиса",       "Потапова",      "student"); // 96
        createUser("9a_rozanov_mi",    "12345678", "Михаил",      "Розанов",       "student"); // 97
        createUser("9a_stepanova_yu",  "12345678", "Юлия",        "Степанова",     "student"); // 98
        createUser("9a_titov_da",      "12345678", "Даниил",      "Титов",         "student"); // 99
        createUser("9a_usov_ri",       "12345678", "Ринат",       "Усов",          "student"); // 100
        createUser("9a_filatova_el",   "12345678", "Елизавета",   "Филатова",      "student"); // 101
        createUser("9a_cherepanov_il", "12345678", "Илья",        "Черепанов",     "student"); // 102
        createUser("9a_shmeleva_ok",   "12345678", "Оксана",      "Шмелёва",       "student"); // 103
        createUser("9a_eliseev_ar",    "12345678", "Арсений",     "Елисеев",       "student"); // 104
        createUser("9a_yartseva_ka",   "12345678", "Карина",      "Ярцева",        "student"); // 105
        createUser("9a_abrahamyan_ar", "12345678", "Арман",       "Абрамян",       "student"); // 106
        createUser("9a_belousov_eg",   "12345678", "Егор",        "Белоусов",      "student"); // 107

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
        UserRole userRole;

        switch (role) {
            case "admin" -> {
                roleId = "fb4ae346-9cbb-4c45-93ad-bd89bfe331a4";
                userRole = UserRole.ADMIN;
            }
            case "teacher" -> {
                roleId = "12535b66-c65b-44fb-a686-49f460f3efba";
                userRole = UserRole.TEACHER;
            }
            case "student" -> {
                roleId = "412fe55a-22e8-494e-98b9-ad47b98abc36";
                userRole = UserRole.STUDENT;
            }
            case "parent" -> {
                roleId = "6696880e-db8e-40a1-9035-eb018ce682e4";
                userRole = UserRole.PARENT;
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }

        User user = userRepository.save(User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .roles(new HashSet<>(Set.of(userRole)))
                .build());

        String keycloakId;
        try {
            keycloakId = keycloakRestClient.createKeyCloakUser(
                    new UserDataDto(username, password, firstName, lastName), user.getId());
        } catch (Exception e) {
            if (e.getMessage().contains("Keycloak User already exists") || e.getMessage().contains("409")) {
                keycloakId = keycloakRestClient.getKeycloakUserByUsername(username).id();
                log.warn("User {} already exists in Keycloak, fetched id={}", username, keycloakId);
            } else {
                throw e;
            }
        }

        try {
            keycloakRestClient.assignRoleToUser(new AssignRoleToUserRequest(keycloakId, userRole.toString(), roleId));
        } catch (Exception ignored) {}

        user.setKeycloakId(keycloakId);
        userRepository.save(user);

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