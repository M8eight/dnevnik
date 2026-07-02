package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.application.mapper.PeriodGradeMapper;
import com.rusobr.academic.application.service.PeriodGradeService;
import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.PeriodGradeProjection;
import com.rusobr.academic.infrastructure.persistence.projection.StudentAverageProjection;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.PeriodGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.StudentAverageDto;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeTeacherResponse;
import com.rusobr.academic.web.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodGradeServiceTest {

    @Mock private PeriodGradeRepository periodGradeRepository;
    @Mock private AcademicPeriodRepository academicPeriodRepository;
    @Mock private TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock private PeriodGradeMapper periodGradeMapper;
    @Mock private UserClient userClient;
    @Mock private TeachingAssignmentService teachingAssignmentService;
    @Mock private GradeRepository gradeRepository;
    @Mock private GradeMapper gradeMapper;

    @InjectMocks private PeriodGradeService service;

    private static final Long STUDENT_ID = 42L;
    private static final Long ASSIGNMENT_ID = 7L;
    private static final Long PERIOD_ID = 1L;
    private static final Long ACADEMIC_YEAR_ID = 1L;

    @Nested
    @DisplayName("getByStudentId")
    class GetByStudentId {

        @Test
        @DisplayName("успешно возвращает сгруппированные по предметам оценки периода")
        void success() {
            PeriodGrade grade1 = new PeriodGrade();
            PeriodGrade grade2 = new PeriodGrade();

            PeriodGradeStudentResponse response1 = new PeriodGradeStudentResponse(1L, 5, "I четверть", "Алгебра", PERIOD_ID);
            PeriodGradeStudentResponse response2 = new PeriodGradeStudentResponse(2L, 4, "II четверть", "Алгебра", PERIOD_ID);
            PeriodGradeStudentResponse response3 = new PeriodGradeStudentResponse(3L, 5, "I четверть", "Физика", PERIOD_ID);

            when(periodGradeRepository.findPeriodGradeByStudentId(STUDENT_ID, 1L))
                    .thenReturn(List.of(grade1, grade1, grade2)); // Передаем 3 сырых сущности

            // Настраиваем маппер на возврат DTO с нужными названиями предметов
            when(periodGradeMapper.toPeriodGradeStudentResponse(grade1))
                    .thenReturn(response1)
                    .thenReturn(response3);
            when(periodGradeMapper.toPeriodGradeStudentResponse(grade2))
                    .thenReturn(response2);

            Map<String, List<PeriodGradeStudentResponse>> result = service.getByStudentId(STUDENT_ID, 1L);

            assertThat(result).hasSize(2);
            assertThat(result.get("Алгебра")).hasSize(2).containsExactly(response1, response2);
            assertThat(result.get("Физика")).hasSize(1).containsExactly(response3);
        }
    }

    @Nested
    @DisplayName("getByAssignmentWithAverage")
    class GetByAssignmentWithAverage {

        @Test
        @DisplayName("успешно собирает данные студентов с итоговыми оценками и средним баллом")
        void success() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 9, 1);
            LocalDate endDate = LocalDate.of(2026, 11, 1);
            AcademicPeriod period = AcademicPeriod.builder().startDate(startDate).endDate(endDate).build();

            UserFeignResponse user = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivan", "key");

            // ИСПРАВЛЕНО: Создаем ОДИН экземпляр сущности, чтобы передавать его в моки по ссылке
            PeriodGrade periodGrade = PeriodGrade.builder()
                    .id(1L)
                    .value(5)
                    .studentId(STUDENT_ID)
                    .build();

            PeriodGradeResponse pgResponse = new PeriodGradeResponse(10L, 5, "I четверть", STUDENT_ID, PERIOD_ID);

            StudentAverageProjection avgProjection = mock(StudentAverageProjection.class);
            StudentAverageDto avgDto = new StudentAverageDto(STUDENT_ID, 4.75);

            when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(period));
            when(teachingAssignmentService.getStudentIdsByTeachingAssignmentId(ASSIGNMENT_ID)).thenReturn(List.of(STUDENT_ID));
            when(userClient.getBatchUsers(List.of(STUDENT_ID))).thenReturn(List.of(user));

            // ИСПРАВЛЕНО: Заменили 1L на константу ACADEMIC_YEAR_ID
            when(periodGradeRepository.findPeriodGradesByTeachingAssignmentId(ASSIGNMENT_ID, ACADEMIC_YEAR_ID))
                    .thenReturn(List.of(periodGrade));

            // ИСПРАВЛЕНО: Передаем ту же самую переменную periodGrade
            when(periodGradeMapper.toPeriodGradeResponse(periodGrade)).thenReturn(pgResponse);

            when(gradeRepository.findAverageStudentsByTeachingAssignment(ASSIGNMENT_ID, startDate, endDate))
                    .thenReturn(List.of(avgProjection));
            when(gradeMapper.toStudentAverageDto(avgProjection)).thenReturn(avgDto);

            // When
            List<PeriodGradeTeacherResponse> result = service.getByAssignmentWithAverage(ASSIGNMENT_ID, PERIOD_ID, ACADEMIC_YEAR_ID);

            // Then
            assertThat(result).hasSize(1);
            PeriodGradeTeacherResponse teacherResponse = result.get(0);
            assertThat(teacherResponse.user()).isEqualTo(user);
            assertThat(teacherResponse.periodGrades()).containsExactly(pgResponse);
            assertThat(teacherResponse.currentAverage()).isEqualTo(4.75);
        }

    @Nested
    @DisplayName("create")
    class Create {
        private final PeriodGradeRequest request = new PeriodGradeRequest(5, "I четверть", ASSIGNMENT_ID, STUDENT_ID, PERIOD_ID);

        @Test
        @DisplayName("успешно создает итоговую оценку, если период открыт")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().id(PERIOD_ID).closed(false).build();
            TeachingAssignment assignment = TeachingAssignment.builder().id(ASSIGNMENT_ID).build();
            PeriodGrade savedGrade = PeriodGrade.builder().id(100L).value(5).build();
            PeriodGradeResponse response = mock(PeriodGradeResponse.class);

            when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(period));
            when(teachingAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
            when(periodGradeRepository.save(any(PeriodGrade.class))).thenReturn(savedGrade);
            when(periodGradeMapper.toPeriodGradeResponse(savedGrade)).thenReturn(response);

            PeriodGradeResponse result = service.create(request);

            assertThat(result).isEqualTo(response);
            verify(periodGradeRepository).save(argThat(pg -> pg.getValue() == 5 && pg.getStudentId().equals(STUDENT_ID)));
        }

        @Test
        @DisplayName("бросает ConflictException, если период закрыт")
        void closedPeriod_throwsException() {
            AcademicPeriod period = AcademicPeriod.builder().closed(true).build();
            when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(period));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Period is already closed");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно удаляет итоговую оценку, если период открыт")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();
            PeriodGrade grade = PeriodGrade.builder().academicPeriod(period).build();

            when(periodGradeRepository.findWithAcademicPeriodById(10L)).thenReturn(Optional.of(grade));

            service.delete(10L);

            verify(periodGradeRepository).delete(grade);
        }

        @Test
        @DisplayName("бросает ConflictException, если период закрыт")
        void closedPeriod_throwsException() {
            AcademicPeriod period = AcademicPeriod.builder().closed(true).build();
            PeriodGrade grade = PeriodGrade.builder().academicPeriod(period).build();

            when(periodGradeRepository.findWithAcademicPeriodById(10L)).thenReturn(Optional.of(grade));

            assertThatThrownBy(() -> service.delete(10L))
                    .isInstanceOf(ConflictException.class);

            verify(periodGradeRepository, never()).delete(any());
        }
    }
}
}