package com.rusobr.academic.securityIT;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.*;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.*;
import com.rusobr.common.dto.UserFeignResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OwnershipSecurityIT extends AbstractSecurityIT {

    private static final long STUDENT_OWNER_ID = 27L;
    private static final long STUDENT_OTHER_ID = 99L;
    private static final long PARENT_ID = 50L;
    private static final long TEACHER_OWNER_ID = 10L;
    private static final long TEACHER_OTHER_ID = 5L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;
    @Autowired
    private ScheduleLessonRepository scheduleLessonRepository;
    @Autowired
    private LessonInstanceRepository lessonInstanceRepository;
    @Autowired
    private GradeRepository gradeRepository;

    @MockitoBean
    private UserClient userClient;

    private long gradeId;
    private long assignmentId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE grades, lesson_instances, schedule_lessons, teaching_assignments,
                        school_classes, academic_years, subjects CASCADE
                """);

        AcademicYear academicYear = academicYearRepository.save(
                AcademicYear.builder()
                        .name("2025-2026")
                        .description("test")
                        .startDate(LocalDate.of(2025, 9, 1))
                        .endDate(LocalDate.of(2026, 5, 31))
                        .build());

        Subject subject = subjectRepository.save(Subject.builder().name("Математика").build());

        SchoolClass schoolClass = schoolClassRepository.save(
                SchoolClass.builder().name("5А").academicYear(academicYear).build());

        TeachingAssignment assignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(TEACHER_OWNER_ID)
                        .schoolClass(schoolClass)
                        .subject(subject)
                        .build());
        assignmentId = assignment.getId();

        ScheduleLesson scheduleLesson = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .teachingAssignment(assignment)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .lessonNumber(1)
                        .validFrom(LocalDate.of(2025, 9, 1))
                        .build());

        LessonInstance lessonInstance = lessonInstanceRepository.save(
                LessonInstance.builder()
                        .scheduleLesson(scheduleLesson)
                        .lessonDate(LocalDate.of(2025, 9, 1))
                        .build());

        gradeId = gradeRepository.save(Grade.builder()
                .studentId(STUDENT_OWNER_ID)
                .lessonInstance(lessonInstance)
                .value(5)
                .weight(2)
                .type(GradeType.TEST)
                .build()).getId();

        when(userClient.getTeacherSimpleById(any())).thenReturn(
                UserFeignResponse.builder()
                        .id(TEACHER_OWNER_ID)
                        .firstName("Иван")
                        .lastName("Учителев")
                        .username("teacher")
                        .build());
    }

    @Test
    @DisplayName("Ученик видит свою оценку -> 200")
    void student_OwnGrade_ShouldReturn200() throws Exception {
        String studentToken = JwtTestUtils.token(STUDENT_OWNER_ID, List.of("STUDENT"));

        mockMvc.perform(get("/api/v1/grades/{id}/detail", gradeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ученик не видит чужую оценку -> 403")
    void student_OtherGrade_ShouldReturn403() throws Exception {
        String otherStudentToken = JwtTestUtils.token(STUDENT_OTHER_ID, List.of("STUDENT"));

        mockMvc.perform(get("/api/v1/grades/{id}/detail", gradeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherStudentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Учитель не видит журнал чужого назначения -> 403")
    void teacher_NotOwnedAssignment_ShouldReturn403() throws Exception {
        String otherTeacherToken = JwtTestUtils.token(TEACHER_OTHER_ID, List.of("TEACHER"));

        mockMvc.perform(get("/api/v1/journal/by-assignment")
                        .param("teachingAssignmentId", String.valueOf(assignmentId))
                        .param("academicPeriodId", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherTeacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Родитель видит оценку своего ребёнка через X-Student-Id -> 200")
    void parent_ChildGrade_ShouldReturn200() throws Exception {
        String parentToken = JwtTestUtils.token(PARENT_ID, List.of("PARENT"));
        when(userClient.isChild(PARENT_ID, STUDENT_OWNER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/v1/grades/{id}/detail", gradeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + parentToken)
                        .header("X-Student-Id", String.valueOf(STUDENT_OWNER_ID)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Родитель без X-Student-Id -> 403")
    void parent_NoStudentHeader_ShouldReturn403() throws Exception {
        String parentToken = JwtTestUtils.token(PARENT_ID, List.of("PARENT"));

        mockMvc.perform(get("/api/v1/grades/{id}/detail", gradeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + parentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Родитель с не привязанным к нему студентом -> 403")
    void parent_UnattachedStudent_ShouldReturn403() throws Exception {
        String parentToken = JwtTestUtils.token(PARENT_ID, List.of("PARENT"));
        when(userClient.isChild(PARENT_ID, STUDENT_OTHER_ID)).thenReturn(false);

        mockMvc.perform(get("/api/v1/grades/{id}/detail", gradeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + parentToken)
                        .header("X-Student-Id", String.valueOf(STUDENT_OTHER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Родитель видит чужую (не своего ребёнка) оценку -> 403")
    void parent_NonChildGrade_ShouldReturn403() throws Exception {
        String parentToken = JwtTestUtils.token(PARENT_ID, List.of("PARENT"));
        when(userClient.isChild(PARENT_ID, STUDENT_OTHER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/v1/grades/{id}/detail", gradeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + parentToken)
                        .header("X-Student-Id", String.valueOf(STUDENT_OTHER_ID)))
                .andExpect(status().isForbidden());
    }

}
