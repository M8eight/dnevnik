package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.AttendanceMapper;
import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.application.mapper.HomeworkMapper;
import com.rusobr.academic.application.mapper.ScheduleLessonMapper;
import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.application.service.ScheduleService;
import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.ScheduleLessonProjection;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolLessonProjection;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.homework.HomeworkSimpleResponse;
import com.rusobr.academic.web.dto.scheduleLesson.*;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryDayDto;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryLessonDto;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryWeekResponse;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.ScheduleLessonDiaryDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentRequest;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.ConflictException;
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
import java.util.Set;

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
    @Mock private AttendanceMapper attendanceMapper;
    @Mock private HomeworkMapper homeworkMapper;
    @Mock private GradeMapper gradeMapper;
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
        @DisplayName("успешно собирает дневник ученика за неделю с оценками, посещаемостью и домашними заданиями")
        void success() {
            LocalDate start = DATE;
            LocalDate end = DATE.plusDays(6);

            ScheduleLesson sl1 = ScheduleLesson.builder().id(SCHEDULE_ID).build();
            Grade grade = Grade.builder()
                    .id(1L)
                    .studentId(STUDENT_ID)
                    .value(5)
                    .weight(1)
                    .type(GradeType.CONTROL)
                    .build();
            Attendance attendance = Attendance.builder()
                    .id(2L)
                    .studentId(STUDENT_ID)
                    .status(AttendanceStatus.LATE)
                    .build();
            Homework homework = Homework.builder()
                    .id(3L)
                    .text("Параграф 5")
                    .build();
            LessonInstance li1 = LessonInstance.builder()
                    .id(50L)
                    .scheduleLesson(sl1)
                    .lessonDate(DATE)
                    .grades(Set.of(grade))
                    .attendances(Set.of(attendance))
                    .homeworks(Set.of(homework))
                    .build();

            GradeResponse gradeResponse = new GradeResponse(1L, STUDENT_ID, 5, 1, GradeType.CONTROL);
            AttendanceSimpleResponse attendanceResponse = new AttendanceSimpleResponse(2L, AttendanceStatus.LATE, STUDENT_ID);
            HomeworkSimpleResponse homeworkResponse = new HomeworkSimpleResponse(3L, "Параграф 5");
            ScheduleLessonDiaryDto scheduleDiaryDto = new ScheduleLessonDiaryDto(
                    SCHEDULE_ID, DayOfWeek.MONDAY, 1, new SubjectResponseDto(7L, "Алгебра"), "101");

            when(scheduleLessonRepository.findDiaryScheduleByStudentId(STUDENT_ID, start, end))
                    .thenReturn(List.of(sl1));
            when(lessonInstanceRepository.findLessonInstancesByScheduleId(List.of(SCHEDULE_ID), start, end))
                    .thenReturn(List.of(li1));
            when(lessonInstanceRepository.findLessonInstanceGradesByPeriodAndStudent(List.of(SCHEDULE_ID), start, end, STUDENT_ID))
                    .thenReturn(List.of(li1));
            when(lessonInstanceRepository.findLessonInstanceAttendancesByPeriodAndStudent(List.of(SCHEDULE_ID), start, end, STUDENT_ID))
                    .thenReturn(List.of(li1));
            when(lessonInstanceRepository.findLessonInstanceHomeworksByPeriodAndStudent(List.of(SCHEDULE_ID), start, end))
                    .thenReturn(List.of(li1));
            when(scheduleLessonMapper.toScheduleLessonDiaryDto(sl1)).thenReturn(scheduleDiaryDto);
            when(gradeMapper.toGradeResponseDto(grade)).thenReturn(gradeResponse);
            when(attendanceMapper.toAttendanceSimpleResponse(attendance)).thenReturn(attendanceResponse);
            when(homeworkMapper.toHomeworkSimpleResponse(homework)).thenReturn(homeworkResponse);

            DiaryWeekResponse result = service.getByStudentId(STUDENT_ID, start, end);

            assertThat(result.weekStart()).isEqualTo(start);
            assertThat(result.weekEnd()).isEqualTo(end);
            assertThat(result.days()).hasSize(1);

            DiaryDayDto day = result.days().get(0);
            assertThat(day.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(day.date()).isEqualTo(DATE);

            DiaryLessonDto lesson = day.lessons().get(0);
            assertThat(lesson).isEqualTo(new DiaryLessonDto(
                    1,
                    new SubjectResponseDto(7L, "Алгебра"),
                    SCHEDULE_ID,
                    50L,
                    "101",
                    List.of(gradeResponse),
                    attendanceResponse,
                    List.of(homeworkResponse)));
        }

        @Test
        @DisplayName("группирует уроки по дате в хронологическом порядке")
        void shouldGroupLessonsByDateChronologically() {
            LocalDate start = DATE;
            LocalDate end = DATE.plusDays(6);

            ScheduleLesson sl1 = ScheduleLesson.builder().id(SCHEDULE_ID).build();
            ScheduleLesson sl2 = ScheduleLesson.builder().id(SCHEDULE_ID + 1).build();
            LessonInstance li1 = LessonInstance.builder()
                    .id(50L)
                    .scheduleLesson(sl1)
                    .lessonDate(DATE)
                    .build();
            LessonInstance li2 = LessonInstance.builder()
                    .id(51L)
                    .scheduleLesson(sl2)
                    .lessonDate(DATE.plusDays(1))
                    .build();

            when(scheduleLessonRepository.findDiaryScheduleByStudentId(STUDENT_ID, start, end))
                    .thenReturn(List.of(sl1, sl2));
            when(lessonInstanceRepository.findLessonInstancesByScheduleId(List.of(SCHEDULE_ID, SCHEDULE_ID + 1), start, end))
                    .thenReturn(List.of(li2, li1));
            when(lessonInstanceRepository.findLessonInstanceGradesByPeriodAndStudent(anyList(), any(), any(), any()))
                    .thenReturn(List.of());
            when(lessonInstanceRepository.findLessonInstanceAttendancesByPeriodAndStudent(anyList(), any(), any(), any()))
                    .thenReturn(List.of());
            when(lessonInstanceRepository.findLessonInstanceHomeworksByPeriodAndStudent(anyList(), any(), any()))
                    .thenReturn(List.of());
            when(scheduleLessonMapper.toScheduleLessonDiaryDto(sl1)).thenReturn(new ScheduleLessonDiaryDto(
                    SCHEDULE_ID, DayOfWeek.MONDAY, 1, new SubjectResponseDto(7L, "Алгебра"), "101"));
            when(scheduleLessonMapper.toScheduleLessonDiaryDto(sl2)).thenReturn(new ScheduleLessonDiaryDto(
                    SCHEDULE_ID + 1, DayOfWeek.TUESDAY, 2, new SubjectResponseDto(8L, "Физика"), "102"));

            DiaryWeekResponse result = service.getByStudentId(STUDENT_ID, start, end);

            assertThat(result.days()).extracting(DiaryDayDto::date)
                    .containsExactly(DATE, DATE.plusDays(1));
            assertThat(result.days()).extracting(DiaryDayDto::dayOfWeek)
                    .containsExactly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        }

        @Test
        @DisplayName("сортирует уроки внутри дня по номеру урока")
        void shouldSortLessonsWithinDayByLessonNumber() {
            LocalDate start = DATE;
            LocalDate end = DATE.plusDays(6);

            ScheduleLesson sl1 = ScheduleLesson.builder().id(SCHEDULE_ID).build();
            ScheduleLesson sl2 = ScheduleLesson.builder().id(SCHEDULE_ID + 1).build();
            LessonInstance li1 = LessonInstance.builder()
                    .id(50L)
                    .scheduleLesson(sl1)
                    .lessonDate(DATE)
                    .build();
            LessonInstance li2 = LessonInstance.builder()
                    .id(51L)
                    .scheduleLesson(sl2)
                    .lessonDate(DATE)
                    .build();

            when(scheduleLessonRepository.findDiaryScheduleByStudentId(STUDENT_ID, start, end))
                    .thenReturn(List.of(sl1, sl2));
            when(lessonInstanceRepository.findLessonInstancesByScheduleId(List.of(SCHEDULE_ID, SCHEDULE_ID + 1), start, end))
                    .thenReturn(List.of(li1, li2));
            when(lessonInstanceRepository.findLessonInstanceGradesByPeriodAndStudent(anyList(), any(), any(), any()))
                    .thenReturn(List.of());
            when(lessonInstanceRepository.findLessonInstanceAttendancesByPeriodAndStudent(anyList(), any(), any(), any()))
                    .thenReturn(List.of());
            when(lessonInstanceRepository.findLessonInstanceHomeworksByPeriodAndStudent(anyList(), any(), any()))
                    .thenReturn(List.of());
            when(scheduleLessonMapper.toScheduleLessonDiaryDto(sl1)).thenReturn(new ScheduleLessonDiaryDto(
                    SCHEDULE_ID, DayOfWeek.MONDAY, 2, new SubjectResponseDto(7L, "Алгебра"), "101"));
            when(scheduleLessonMapper.toScheduleLessonDiaryDto(sl2)).thenReturn(new ScheduleLessonDiaryDto(
                    SCHEDULE_ID + 1, DayOfWeek.MONDAY, 1, new SubjectResponseDto(8L, "Физика"), "102"));

            DiaryWeekResponse result = service.getByStudentId(STUDENT_ID, start, end);

            assertThat(result.days()).hasSize(1);
            assertThat(result.days().get(0).lessons()).extracting(DiaryLessonDto::lessonNumber)
                    .containsExactly(1, 2);
        }

        @Test
        @DisplayName("урок без оценок, посещаемости и домашних заданий маппится с пустыми коллекциями")
        void shouldMapEmptyGradesAttendancesAndHomeworks() {
            LocalDate start = DATE;
            LocalDate end = DATE.plusDays(6);

            ScheduleLesson sl1 = ScheduleLesson.builder().id(SCHEDULE_ID).build();
            LessonInstance li1 = LessonInstance.builder()
                    .id(50L)
                    .scheduleLesson(sl1)
                    .lessonDate(DATE)
                    .build();

            when(scheduleLessonRepository.findDiaryScheduleByStudentId(STUDENT_ID, start, end))
                    .thenReturn(List.of(sl1));
            when(lessonInstanceRepository.findLessonInstancesByScheduleId(List.of(SCHEDULE_ID), start, end))
                    .thenReturn(List.of(li1));
            when(lessonInstanceRepository.findLessonInstanceGradesByPeriodAndStudent(List.of(SCHEDULE_ID), start, end, STUDENT_ID))
                    .thenReturn(List.of());
            when(lessonInstanceRepository.findLessonInstanceAttendancesByPeriodAndStudent(List.of(SCHEDULE_ID), start, end, STUDENT_ID))
                    .thenReturn(List.of());
            when(lessonInstanceRepository.findLessonInstanceHomeworksByPeriodAndStudent(List.of(SCHEDULE_ID), start, end))
                    .thenReturn(List.of());
            when(scheduleLessonMapper.toScheduleLessonDiaryDto(sl1)).thenReturn(new ScheduleLessonDiaryDto(
                    SCHEDULE_ID, DayOfWeek.MONDAY, 1, new SubjectResponseDto(7L, "Алгебра"), "101"));

            DiaryWeekResponse result = service.getByStudentId(STUDENT_ID, start, end);

            DiaryLessonDto lesson = result.days().get(0).lessons().get(0);
            assertThat(lesson.grades()).isEmpty();
            assertThat(lesson.attendance()).isNull();
            assertThat(lesson.homeworks()).isEmpty();
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
            when(userClient.getBatchTeachers(List.of(TEACHER_ID))).thenReturn(new BatchUserResponse(List.of(teacherResponse), List.of(), false));
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
            when(scheduleLessonRepository.existsByTeacherSlot(TEACHER_ID, DayOfWeek.MONDAY, 1, DATE)).thenReturn(false);
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
