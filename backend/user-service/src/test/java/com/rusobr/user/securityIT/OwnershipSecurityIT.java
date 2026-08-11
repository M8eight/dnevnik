package com.rusobr.user.securityIT;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.feign.AcademicClient;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.feign.AcademicYearResponse;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OwnershipSecurityIT extends AbstractSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private AcademicClient academicClient;

    private Long studentOwnerId;
    private Long studentOtherId;
    private Long teacherId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE user_roles, students, teachers, parents, users CASCADE
                """);
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        transactionTemplate.executeWithoutResult(status -> {
            User ownerUser = userRepository.save(User.builder()
                    .username("student_owner")
                    .firstName("Иван")
                    .lastName("Иванов")
                    .roles(Set.of(UserRole.STUDENT))
                    .build());
            studentOwnerId = ownerUser.getId();
            studentRepository.save(Student.builder()
                    .user(ownerUser)
                    .studyProfile("Социо-эконом")
                    .build());

            User otherUser = userRepository.save(User.builder()
                    .username("student_other")
                    .firstName("Пётр")
                    .lastName("Петров")
                    .roles(Set.of(UserRole.STUDENT))
                    .build());
            studentOtherId = otherUser.getId();
            studentRepository.save(Student.builder()
                    .user(otherUser)
                    .studyProfile("Физ-мат")
                    .build());

            User teacherUser = userRepository.save(User.builder()
                    .username("teacher_owner")
                    .firstName("Анна")
                    .lastName("Учителева")
                    .roles(Set.of(UserRole.TEACHER))
                    .build());
            teacherId = teacherUser.getId();
            teacherRepository.save(Teacher.builder()
                    .user(teacherUser)
                    .email("anna@school.ru")
                    .phoneNumber("+7-900-000-0000")
                    .build());
        });

        when(academicClient.getSchoolClassByStudentId(any())).thenReturn(
                new SchoolClassResponse(1L, "5А",
                        new AcademicYearResponse(1L, "2025-2026", "test",
                                LocalDate.of(2025, 9, 1), LocalDate.of(2026, 5, 31), true),
                        teacherId));
    }

    @Test
    @DisplayName("Ученик получает свои данные (with-class берёт user_id из JWT) -> 200")
    void student_OwnData_ShouldReturn200() throws Exception {
        String studentToken = JwtTestUtils.token(studentOwnerId, List.of("STUDENT"));

        mockMvc.perform(get("/api/v1/students/with-class")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ученик не видит данные другого ученика (info доступен только ADMIN/TEACHER) -> 403")
    void student_OtherStudentData_ShouldReturn403() throws Exception {
        String studentToken = JwtTestUtils.token(studentOwnerId, List.of("STUDENT"));

        mockMvc.perform(get("/api/v1/students/{id}/info", studentOtherId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Учитель с валидным токеном не имеет доступа к ADMIN-скоупу -> 403")
    void teacher_NotAdminScope_ShouldReturn403() throws Exception {
        String teacherToken = JwtTestUtils.token(teacherId, List.of("TEACHER"));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

}
