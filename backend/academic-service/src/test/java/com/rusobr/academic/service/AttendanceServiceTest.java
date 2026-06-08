package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.AttendanceMapper;
import com.rusobr.academic.application.service.AttendanceService;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.AttendanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private AttendanceMapper attendanceMapper;
    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private AcademicPeriodRepository academicPeriodRepository;

    @InjectMocks private AttendanceService service;

    private static final Long ATTENDANCE_ID = 1L;
    private static final Long STUDENT_ID = 42L;
    private static final Long LESSON_INSTANCE_ID = 100L;
    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 9, 1);

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("lesson instance не найден — бросает NotFoundException")
        void lessonInstanceNotFound_throwsException() {
            AttendanceRequest request = new AttendanceRequest(STUDENT_ID, AttendanceStatus.ABSENT, LESSON_INSTANCE_ID);
            when(lessonInstanceRepository.findById(LESSON_INSTANCE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Lesson Instance Not Found" + LESSON_INSTANCE_ID);

            verifyNoInteractions(academicPeriodRepository, attendanceRepository, attendanceMapper);
        }

        @Test
        @DisplayName("академический период для даты урока не найден — бросает NotFoundException")
        void academicPeriodNotFound_throwsException() {
            AttendanceRequest request = new AttendanceRequest(STUDENT_ID, AttendanceStatus.ABSENT, LESSON_INSTANCE_ID);
            LessonInstance lessonInstance = LessonInstance.builder().id(LESSON_INSTANCE_ID).lessonDate(LESSON_DATE).build();

            when(lessonInstanceRepository.findById(LESSON_INSTANCE_ID)).thenReturn(Optional.of(lessonInstance));
            when(academicPeriodRepository.findByDate(LESSON_DATE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic Period Not Found");

            verifyNoInteractions(attendanceRepository, attendanceMapper);
        }

        @Test
        @DisplayName("академический период закрыт — бросает ConflictException")
        void academicPeriodClosed_throwsConflictException() {
            AttendanceRequest request = new AttendanceRequest(STUDENT_ID, AttendanceStatus.ABSENT, LESSON_INSTANCE_ID);
            LessonInstance lessonInstance = LessonInstance.builder().id(LESSON_INSTANCE_ID).lessonDate(LESSON_DATE).build();
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

            when(lessonInstanceRepository.findById(LESSON_INSTANCE_ID)).thenReturn(Optional.of(lessonInstance));
            when(academicPeriodRepository.findByDate(LESSON_DATE)).thenReturn(Optional.of(closedPeriod));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic Period is closed");

            verifyNoInteractions(attendanceRepository, attendanceMapper);
        }

        @Test
        @DisplayName("успешный upsert: запись существовала -> обновляет статус и сохраняет")
        void success_existingAttendanceUpdated() {
            AttendanceRequest request = new AttendanceRequest(STUDENT_ID, AttendanceStatus.LATE, LESSON_INSTANCE_ID);
            LessonInstance lessonInstance = LessonInstance.builder().id(LESSON_INSTANCE_ID).lessonDate(LESSON_DATE).build();
            AcademicPeriod openPeriod = AcademicPeriod.builder().closed(false).build();

            Attendance existingAttendance = Attendance.builder()
                    .id(ATTENDANCE_ID)
                    .studentId(STUDENT_ID)
                    .status(AttendanceStatus.ABSENT) // старый статус
                    .lessonInstance(lessonInstance)
                    .build();

            AttendanceResponse expectedResponse = mock(AttendanceResponse.class);

            when(lessonInstanceRepository.findById(LESSON_INSTANCE_ID)).thenReturn(Optional.of(lessonInstance));
            when(academicPeriodRepository.findByDate(LESSON_DATE)).thenReturn(Optional.of(openPeriod));
            when(attendanceRepository.findByStudentIdAndLessonInstanceId(STUDENT_ID, LESSON_INSTANCE_ID))
                    .thenReturn(Optional.of(existingAttendance));
            when(attendanceRepository.save(existingAttendance)).thenReturn(existingAttendance);
            when(attendanceMapper.toAttendanceResponse(existingAttendance)).thenReturn(expectedResponse);

            AttendanceResponse result = service.create(request);

            assertThat(result).isNotNull().isEqualTo(expectedResponse);
            assertThat(existingAttendance.getStatus()).isEqualTo(AttendanceStatus.LATE); // Проверяем мутацию статуса
            verify(attendanceMapper, never()).toAttendance(any(), any()); // Фабричный метод маппера не должен вызываться
        }

        @Test
        @DisplayName("успешный upsert: записи не было -> маппит новую сущность и сохраняет")
        void success_newAttendanceCreated() {
            AttendanceRequest request = new AttendanceRequest(STUDENT_ID, AttendanceStatus.EXCUSED, LESSON_INSTANCE_ID);
            LessonInstance lessonInstance = LessonInstance.builder().id(LESSON_INSTANCE_ID).lessonDate(LESSON_DATE).build();
            AcademicPeriod openPeriod = AcademicPeriod.builder().closed(false).build();

            Attendance newAttendance = Attendance.builder()
                    .studentId(STUDENT_ID)
                    .status(AttendanceStatus.EXCUSED)
                    .lessonInstance(lessonInstance)
                    .build();

            Attendance savedAttendance = Attendance.builder()
                    .id(ATTENDANCE_ID)
                    .studentId(STUDENT_ID)
                    .status(AttendanceStatus.EXCUSED)
                    .lessonInstance(lessonInstance)
                    .build();

            AttendanceResponse expectedResponse = mock(AttendanceResponse.class);

            when(lessonInstanceRepository.findById(LESSON_INSTANCE_ID)).thenReturn(Optional.of(lessonInstance));
            when(academicPeriodRepository.findByDate(LESSON_DATE)).thenReturn(Optional.of(openPeriod));
            when(attendanceRepository.findByStudentIdAndLessonInstanceId(STUDENT_ID, LESSON_INSTANCE_ID))
                    .thenReturn(Optional.empty());
            when(attendanceMapper.toAttendance(request, lessonInstance)).thenReturn(newAttendance);
            when(attendanceRepository.save(newAttendance)).thenReturn(savedAttendance);
            when(attendanceMapper.toAttendanceResponse(savedAttendance)).thenReturn(expectedResponse);

            AttendanceResponse result = service.create(request);

            assertThat(result).isNotNull().isEqualTo(expectedResponse);
            verify(attendanceMapper).toAttendance(request, lessonInstance);
            verify(attendanceRepository).save(newAttendance);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно вызывает удаление из репозитория по id")
        void success() {
            service.delete(ATTENDANCE_ID);

            verify(attendanceRepository).deleteById(ATTENDANCE_ID);
        }
    }
}