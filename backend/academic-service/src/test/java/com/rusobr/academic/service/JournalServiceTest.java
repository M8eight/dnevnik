package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.application.service.AcademicPeriodService;
import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.AttendanceStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.LessonInstanceProjection;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.*;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private AcademicPeriodMapper academicPeriodMapper;
    @Mock private SchoolClassRepository schoolClassRepository;
    @Mock private UserClient userClient;
    @Mock private LessonInstanceMapper lessonInstanceMapper;
    @Mock private AcademicPeriodService academicPeriodService;
    @Mock private TransactionTemplate readOnlyTransactionTemplate;

    @InjectMocks private JournalService journalService;

    private static final Long PERIOD_ID = 1L;
    private static final Long STUDENT_ID = 42L;
    private static final Long ASSIGNMENT_ID = 7L;
    private static final Long LESSON_INSTANCE_ID = 100L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 30);

    @SuppressWarnings("unchecked")
    private void stubReadOnlyTransactionTemplate() {
        lenient().when(readOnlyTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

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

            GradesLessonsResponse result = journalService.getGradesByStudentId(STUDENT_ID, PERIOD_ID);

            assertThat(result).isNotNull();
            assertThat(result.dates()).isEqualTo(dates);
            assertThat(result.gradesBySubjects()).hasSize(1);
            assertThat(result.gradesBySubjects().get(0).subject()).isEqualTo("Математика");
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void periodNotFound_throwsNotFoundException() {
            when(academicPeriodService.getById(PERIOD_ID))
                    .thenThrow(new NotFoundException("Academic period with id " + PERIOD_ID + " not found", null));

            assertThatThrownBy(() -> journalService.getGradesByStudentId(STUDENT_ID, PERIOD_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getJournalByAssignment")
    class GetJournalByAssignment {

        @Test
        @DisplayName("успешно собирает журнал учителя и корректно считает средневзвешенный балл")
        void success() {
            stubReadOnlyTransactionTemplate();
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
            StudentJournalDto.GradeLessonTeacherDto mockGradeDto = mock(StudentJournalDto.GradeLessonTeacherDto.class);
            when(mockGradeDto.studentId()).thenReturn(STUDENT_ID);
            when(mockGradeDto.value()).thenReturn(5);
            when(mockGradeDto.weight()).thenReturn(2);
            when(mockGradeDto.lessonInstanceId()).thenReturn(LESSON_INSTANCE_ID);

            StudentJournalDto.AttendanceLessonTeacherDto mockAttendanceDto = mock(StudentJournalDto.AttendanceLessonTeacherDto.class);
            when(mockAttendanceDto.studentId()).thenReturn(STUDENT_ID);
            when(mockAttendanceDto.lessonInstanceId()).thenReturn(LESSON_INSTANCE_ID);

            // Обучаем репозитории возвращать списки проекций
            when(academicPeriodService.getById(PERIOD_ID)).thenReturn(period);
            when(lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(lessonProjection));
            when(lessonInstanceMapper.toLessonInstanceDto(lessonProjection)).thenReturn(lessonDto);

            when(schoolClassRepository.findStudentsIdsByTeachingAssignment(ASSIGNMENT_ID)).thenReturn(studentIds);
            when(userClient.getBatchStudents(studentIds)).thenReturn(new BatchUserResponse(List.of(studentFeign), List.of(), false));

            when(lessonInstanceRepository.findGradesByTeachingAssignment(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(gradeProjection));
            when(lessonInstanceMapper.toGradeStudentDto(gradeProjection)).thenReturn(mockGradeDto);

            when(lessonInstanceRepository.findAttendancesByTeachingAssignment(ASSIGNMENT_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(attendanceProjection));
            when(lessonInstanceMapper.toAttendanceStudentDto(attendanceProjection)).thenReturn(mockAttendanceDto);

            when(academicPeriodMapper.toResponse(period)).thenReturn(periodResponse);

            TeacherJournalResponse result = journalService.getJournalByAssignment(ASSIGNMENT_ID, PERIOD_ID);

            assertThat(result).isNotNull();
            assertThat(result.studentsJournal()).hasSize(1);

            StudentJournalDto journalRow = result.studentsJournal().get(0);
            assertThat(journalRow.student()).isEqualTo(studentFeign);
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

            List<LessonInstanceDto> result = journalService.getInstancesByAssignment(ASSIGNMENT_ID, PERIOD_ID);

            assertThat(result).hasSize(1).contains(lessonDto);
        }
    }
}
