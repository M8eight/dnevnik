package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.application.service.AcademicPeriodService;
import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.AttendanceStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.LessonInstanceProjection;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.*;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private AcademicPeriodRepository academicPeriodRepository;
    @Mock private AcademicPeriodMapper academicPeriodMapper;
    @Mock private SchoolClassRepository schoolClassRepository;
    @Mock private UserClient userClient;
    @Mock private LessonInstanceMapper lessonInstanceMapper;
    @Mock private AcademicPeriodService academicPeriodService;

    @InjectMocks private JournalService service;

    private static final Long PERIOD_ID = 1L;
    private static final Long STUDENT_ID = 42L;
    private static final Long ASSIGNMENT_ID = 7L;
    private static final Long LESSON_INSTANCE_ID = 100L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 30);

    @Nested
    @DisplayName("getGradesLessonsByStudentId")
    class GetGradesLessonsByStudentId {

        @Test
        @DisplayName("успешно группирует оценки по предметам и возвращает структуру журнала ученика")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().startDate(START_DATE).endDate(END_DATE).build();
            AcademicPeriodResponse periodResponse = mock(AcademicPeriodResponse.class);

            // Создаем мок проекции с правильным типом
            GradeJournalProjection gradeJournalProjection = mock(GradeJournalProjection.class);
            GradeJournalDto journalDto = new GradeJournalDto("Математика", 10L, 5, 1, GradeType.CONTROL, START_DATE);
            List<LocalDate> dates = List.of(START_DATE);

            when(academicPeriodService.getById(PERIOD_ID)).thenReturn(period);
            when(lessonInstanceRepository.findGradesLessonsByStudentId(STUDENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(gradeJournalProjection));
            when(lessonInstanceMapper.toGradeJournalProjection(gradeJournalProjection)).thenReturn(journalDto);
            when(lessonInstanceRepository.findLessonDatesByStudentId(STUDENT_ID, START_DATE, END_DATE))
                    .thenReturn(dates);
            when(academicPeriodMapper.toResponse(period)).thenReturn(periodResponse);

            GradesLessonsResponse result = service.getGradesLessonsByStudentId(STUDENT_ID, PERIOD_ID);

            assertThat(result).isNotNull();
            assertThat(result.dates()).isEqualTo(dates);
            assertThat(result.subjects()).hasSize(1);
            assertThat(result.subjects().get(0).subject()).isEqualTo("Математика");
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void periodNotFound_throwsNotFoundException() {
            when(academicPeriodService.getById(PERIOD_ID))
                    .thenThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found", null));

            assertThatThrownBy(() -> service.getGradesLessonsByStudentId(STUDENT_ID, PERIOD_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getJournalByAssignment")
    class GetJournalByAssignment {

        @Test
        @DisplayName("успешно собирает журнал учителя и корректно считает средневзвешенный балл")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().startDate(START_DATE).endDate(END_DATE).build();
            AcademicPeriodResponse periodResponse = mock(AcademicPeriodResponse.class);

            // Мокаем проекции из репозитория
            LessonInstanceProjection lessonProjection = mock(LessonInstanceProjection.class);
            GradeStudentProjection gradeProjection = mock(GradeStudentProjection.class);
            AttendanceStudentProjection attendanceProjection = mock(AttendanceStudentProjection.class);

            LessonInstanceDto lessonDto = new LessonInstanceDto(LESSON_INSTANCE_ID, START_DATE);
            List<Long> studentIds = List.of(STUDENT_ID);
            UserFeignResponse studentFeign = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivan", "key");

            // Настраиваем внутреннее поведение DTO-шек, так как сервис будет вызывать их геттеры
            GradeStudentDto mockGradeDto = mock(GradeStudentDto.class);
            when(mockGradeDto.studentId()).thenReturn(STUDENT_ID);
            when(mockGradeDto.gradeId()).thenReturn(20L);
            when(mockGradeDto.value()).thenReturn(5);
            when(mockGradeDto.weight()).thenReturn(2);
            when(mockGradeDto.gradeType()).thenReturn(GradeType.CONTROL);
            when(mockGradeDto.lessonInstanceId()).thenReturn(LESSON_INSTANCE_ID);

            AttendanceStudentDto mockAttendanceDto = mock(AttendanceStudentDto.class);
            when(mockAttendanceDto.studentId()).thenReturn(STUDENT_ID);
            when(mockAttendanceDto.attendanceId()).thenReturn(30L);
            when(mockAttendanceDto.status()).thenReturn(AttendanceStatus.LATE);
            when(mockAttendanceDto.lessonInstanceId()).thenReturn(LESSON_INSTANCE_ID);

            // Обучаем репозитории возвращать списки проекций
            when(academicPeriodService.getById(PERIOD_ID)).thenReturn(period);
            when(lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(lessonProjection));
            when(lessonInstanceMapper.toLessonInstanceDto(lessonProjection)).thenReturn(lessonDto);

            when(schoolClassRepository.findStudentsIdsByTeachingAssignment(ASSIGNMENT_ID)).thenReturn(studentIds);
            when(userClient.getBatchUsers(studentIds)).thenReturn(new BatchUserResponse(List.of(studentFeign), List.of()));

            when(lessonInstanceRepository.findGradesByTeachingAssignment(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(gradeProjection));
            when(lessonInstanceMapper.toGradeStudentDto(gradeProjection)).thenReturn(mockGradeDto);

            when(lessonInstanceRepository.findAttendancesByTeachingAssignment(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(attendanceProjection));
            when(lessonInstanceMapper.toAttendanceStudentDto(attendanceProjection)).thenReturn(mockAttendanceDto);

            when(academicPeriodMapper.toResponse(period)).thenReturn(periodResponse);

            TeacherJournalResponse result = service.getJournalByAssignment(ASSIGNMENT_ID, PERIOD_ID);

            assertThat(result).isNotNull();
            assertThat(result.studentsJournal()).hasSize(1);

            StudentJournalDto journalRow = result.studentsJournal().get(0);
            assertThat(journalRow.studentId()).isEqualTo(STUDENT_ID);
            assertThat(journalRow.gradesAverage()).isEqualTo(5.0); // (5*2)/2 = 5.0
        }
    }

    @Nested
    @DisplayName("getInstancesByAssignment")
    class GetInstancesByAssignment {

        @Test
        @DisplayName("успешно возвращает список LessonInstanceDto для назначения")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().startDate(START_DATE).endDate(END_DATE).build();
            LessonInstanceProjection lessonProjection = mock(LessonInstanceProjection.class);
            LessonInstanceDto lessonDto = new LessonInstanceDto(LESSON_INSTANCE_ID, START_DATE);

            when(academicPeriodService.getById(PERIOD_ID)).thenReturn(period);
            when(lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(lessonProjection));
            when(lessonInstanceMapper.toLessonInstanceDto(lessonProjection)).thenReturn(lessonDto);

            List<LessonInstanceDto> result = service.getInstancesByAssignment(ASSIGNMENT_ID, PERIOD_ID);

            assertThat(result).hasSize(1).contains(lessonDto);
        }
    }

    @Nested
    @DisplayName("generateInstanceForLesson")
    class GenerateInstanceForLesson {

        @Test
        @DisplayName("успешно генерирует уроки на две недели вперед с проверкой на существование")
        void success_generatesForTwoWeeks() {
            LocalDate validFrom = LocalDate.of(2026, 6, 1);
            ScheduleLesson scheduleLesson = ScheduleLesson.builder()
                    .validFrom(validFrom)
                    .validTo(null)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .build();

            LocalDate date1 = LocalDate.of(2026, 6, 1);
            LocalDate date2 = LocalDate.of(2026, 6, 8);
            LocalDate date3 = LocalDate.of(2026, 6, 15);

            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(scheduleLesson, date1)).thenReturn(false);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(scheduleLesson, date2)).thenReturn(true);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(scheduleLesson, date3)).thenReturn(false);

            service.generateInstanceForLesson(scheduleLesson);

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(date1)));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(date3)));
            verify(lessonInstanceRepository, times(2)).save(any(LessonInstance.class));
        }
    }
}
