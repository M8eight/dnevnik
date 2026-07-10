package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.application.mapper.ScheduleLessonMapper;
import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.application.service.ScheduleService;
import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.ScheduleLessonProjection;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolLessonProjection;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.homework.HomeworkDiaryResponse;
import com.rusobr.academic.web.dto.lessonInstance.DiaryLessonInstanceDto;
import com.rusobr.academic.web.dto.scheduleLesson.*;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentRequest;
import com.rusobr.academic.web.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleLessonRepository scheduleLessonRepository;
    @Mock private UserClient userClient;
    @Mock private ScheduleLessonMapper scheduleLessonMapper;
    @Mock private TeachingAssignmentService teachingAssignmentService;
    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private JournalService lessonInstanceService;
    @Mock private LessonInstanceMapper lessonInstanceMapper;
    @Mock private ScheduleService self;

    @InjectMocks private ScheduleService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", self);
    }

    private static final Long STUDENT_ID = 1L;
    private static final Long CLASS_ID = 10L;
    private static final Long TEACHER_ID = 5L;
    private static final Long SCHEDULE_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 6, 8);

    @Nested
    @DisplayName("getByDate")
    class GetByDate {
        @Test
        @DisplayName("успешно возвращает список расписания по дате")
        void success() {
            ScheduleLessonProjection projection = mock(ScheduleLessonProjection.class);
            ScheduleLessonResponse response = new ScheduleLessonResponse(SCHEDULE_ID, 1, "Математика", "Каб. 23");

            when(scheduleLessonRepository.getScheduleByDate(STUDENT_ID, DayOfWeek.MONDAY, DATE))
                    .thenReturn(List.of(projection));
            when(scheduleLessonMapper.toScheduleLessonResponse(projection)).thenReturn(response);

            List<ScheduleLessonResponse> result = service.getByDate(STUDENT_ID, DATE);

            assertThat(result).hasSize(1).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("getByStudentId")
    class GetByStudentId {
        @Test
        @DisplayName("успешно собирает дневник ученика, фильтруя чужие оценки и сортируя по дням недели")
        void success() {
            LocalDate start = DATE;
            LocalDate end = DATE.plusDays(6);

            ScheduleLesson sl1 = ScheduleLesson.builder().id(SCHEDULE_ID).build();
            LessonInstance li1 = mock(LessonInstance.class);

            // Оценки для текущего ученика и чужого
            AttendanceSimpleResponse att = new AttendanceSimpleResponse(1L, AttendanceStatus.LATE, STUDENT_ID);
            AttendanceSimpleResponse alienAtt = new AttendanceSimpleResponse(2L, AttendanceStatus.ABSENT, 999L);
            GradeResponse grade = new GradeResponse(1L, STUDENT_ID, 5, 1, GradeType.CONTROL);
            HomeworkDiaryResponse homework = new HomeworkDiaryResponse(1L, "Параграф 5");

            DiaryLessonInstanceDto initialDto = new DiaryLessonInstanceDto(
                    50L, SCHEDULE_ID, DATE,
                    List.of(att, alienAtt), List.of(grade), homework
            );

            DiaryScheduleDto expectedDto = new DiaryScheduleDto(
                    SCHEDULE_ID, DayOfWeek.MONDAY, 1, "101",
                    start, end, null, null
            );

            when(scheduleLessonRepository.findDiaryScheduleByStudentId(STUDENT_ID, start, end))
                    .thenReturn(List.of(sl1));
            when(lessonInstanceRepository.findDiaryAcademicPerformanceByStudentId(List.of(SCHEDULE_ID), start, end, STUDENT_ID))
                    .thenReturn(List.of(li1));
            when(lessonInstanceMapper.toDiaryLessonInstance(li1)).thenReturn(initialDto);

            // Заставляем маппер вернуть DTO
            when(scheduleLessonMapper.toDiaryScheduleDto(eq(sl1), any(DiaryLessonInstanceDto.class)))
                    .thenReturn(expectedDto);

            List<DiaryScheduleDto> result = service.getByStudentId(STUDENT_ID, start, end);

            assertThat(result).hasSize(1);
            // Проверяем, что в результирующем инстансе осталась только запись нашего студента (alienAtt отсеялся)
            verify(scheduleLessonMapper).toDiaryScheduleDto(eq(sl1), argThat(dto ->
                    dto.attendances().size() == 1 && dto.attendances().get(0).studentId().equals(STUDENT_ID)
            ));
        }
    }

    @Nested
    @DisplayName("getWeekSchedule")
    class GetWeekSchedule {
        @Test
        @DisplayName("возвращает расписание недели, сгруппированное по дням и отсортированное по номеру урока")
        void success() {
            SchoolLessonProjection p1 = mock(SchoolLessonProjection.class);
            SchoolLessonProjection p2 = mock(SchoolLessonProjection.class);

            SchoolLessonResponse r1 = new SchoolLessonResponse(1L, 2, "Физика", "102", DayOfWeek.TUESDAY);
            SchoolLessonResponse r2 = new SchoolLessonResponse(2L, 1, "Химия", "103", DayOfWeek.TUESDAY);

            when(scheduleLessonRepository.findAllByStudentId(STUDENT_ID)).thenReturn(List.of(p1, p2));
            when(scheduleLessonMapper.toSchoolLessonResponse(p1)).thenReturn(r1);
            when(scheduleLessonMapper.toSchoolLessonResponse(p2)).thenReturn(r2);

            Map<DayOfWeek, List<SchoolLessonResponse>> result = service.getWeekSchedule(STUDENT_ID);

            assertThat(result).containsKey(DayOfWeek.TUESDAY);
            // Проверяем сортировку по lessonNumber: сначала Химия (1), потом Физика (2)
            assertThat(result.get(DayOfWeek.TUESDAY)).containsExactly(r2, r1);
        }
    }

    @Nested
    @DisplayName("getByClass")
    class GetByClass {
        @Test
        @DisplayName("успешно возвращает расписание класса с обогащением данных об учителях")
        void success() {
            TeachingAssignment ta = mock(TeachingAssignment.class);
            when(ta.getTeacherId()).thenReturn(TEACHER_ID);
            ScheduleLesson sl = ScheduleLesson.builder().teachingAssignment(ta).build();

            UserFeignResponse teacherResponse = new UserFeignResponse(TEACHER_ID, "Петр", "Петров", "petr", "id");
            ScheduleLessonDto dto = mock(ScheduleLessonDto.class);
            when(dto.dayOfWeek()).thenReturn(DayOfWeek.WEDNESDAY);

            when(scheduleLessonRepository.findClassSchedule(CLASS_ID, DATE)).thenReturn(List.of(sl));
            when(userClient.getBatchTeachers(List.of(TEACHER_ID))).thenReturn(new BatchUserResponse(List.of(teacherResponse), List.of()));
            when(scheduleLessonMapper.toDto(sl, teacherResponse)).thenReturn(dto);

            Map<DayOfWeek, List<ScheduleLessonDto>> result = service.getByClass(CLASS_ID, DATE);

            assertThat(result).containsKey(DayOfWeek.WEDNESDAY);
            assertThat(result.get(DayOfWeek.WEDNESDAY)).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("валидирует существование учителя и делегирует транзакционное создание")
        void success() {
            ScheduleLessonRequest request = new ScheduleLessonRequest(CLASS_ID, 2L, TEACHER_ID, DayOfWeek.MONDAY, 1, "101", DATE);

            service.create(request);

            verify(userClient).getTeacherById(TEACHER_ID);
            verify(self).createTransactional(request);
        }
    }

    @Nested
    @DisplayName("createTransactional")
    class CreateTransactional {
        private final ScheduleLessonRequest request = new ScheduleLessonRequest(CLASS_ID, 2L, TEACHER_ID, DayOfWeek.MONDAY, 1, "101", DATE);

        @Test
        @DisplayName("успешно сохраняет слот и запускает генерацию уроков")
        void success() {
            TeachingAssignment ta = TeachingAssignment.builder().id(88L).build();
            ScheduleLesson sl = new ScheduleLesson();

            when(teachingAssignmentService.createOrGet(any(TeachingAssignmentRequest.class))).thenReturn(ta);
            when(scheduleLessonRepository.existsActiveByClassSlot(CLASS_ID, DayOfWeek.MONDAY, 1, DATE)).thenReturn(false);
            when(scheduleLessonRepository.existsActiveByTeachingAssignmentSlot(88L, DayOfWeek.MONDAY, 1, DATE)).thenReturn(false);
            when(scheduleLessonMapper.toEntity(request, ta)).thenReturn(sl);

            service.createTransactional(request);

            verify(scheduleLessonRepository).save(sl);
            verify(lessonInstanceService).generateInstanceForLesson(sl);
        }

        @Test
        @DisplayName("бросает ConflictException, если слот класса уже занят")
        void classSlotTaken_throwsConflictException() {
            TeachingAssignment ta = TeachingAssignment.builder().id(88L).build();
            when(teachingAssignmentService.createOrGet(any(TeachingAssignmentRequest.class))).thenReturn(ta);
            when(scheduleLessonRepository.existsActiveByClassSlot(CLASS_ID, DayOfWeek.MONDAY, 1, DATE)).thenReturn(true);

            assertThatThrownBy(() -> service.createTransactional(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Slot is already taken for this class");
        }
    }

    @Nested
    @DisplayName("close")
    class Close {
        @Test
        @DisplayName("успешно закрывает расписание и инициирует мягкое удаление пустых уроков в будущем")
        void success() {
            ScheduleLesson sl = ScheduleLesson.builder().id(SCHEDULE_ID).validTo(null).build();
            when(scheduleLessonRepository.findWithTeachingAssignmentById(SCHEDULE_ID)).thenReturn(Optional.of(sl));

            service.close(SCHEDULE_ID, DATE);

            assertThat(sl.getValidTo()).isEqualTo(DATE);
            verify(scheduleLessonRepository).save(sl);
            verify(lessonInstanceRepository).softDeleteFutureEmptyAfterDate(SCHEDULE_ID, DATE);
        }

        @Test
        @DisplayName("бросает ConflictException, если расписание уже закрыто")
        void alreadyClosed_throwsConflictException() {
            // validTo в прошлом (относительно 2026 года)
            ScheduleLesson sl = ScheduleLesson.builder().id(SCHEDULE_ID).validTo(LocalDate.of(2025, 1, 1)).build();
            when(scheduleLessonRepository.findWithTeachingAssignmentById(SCHEDULE_ID)).thenReturn(Optional.of(sl));

            assertThatThrownBy(() -> service.close(SCHEDULE_ID, DATE))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("is already closed");
        }
    }

    @Nested
    @DisplayName("load")
    class Load {
        @Test
        @DisplayName("выбирает все уроки за период и генерирует для них экземпляры")
        void success() {
            LocalDate to = DATE.plusWeeks(1);
            ScheduleLesson sl = new ScheduleLesson();
            when(scheduleLessonRepository.findAllByClassIdAndPeriod(CLASS_ID, DATE, to)).thenReturn(List.of(sl));

            service.load(CLASS_ID, DATE, to);

            verify(lessonInstanceService).generateInstanceBetween(sl, DATE, to);
        }
    }
}
