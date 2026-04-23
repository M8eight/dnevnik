package com.rusobr.academic.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.PeriodGradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.PeriodGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.infrastructure.service.PeriodGradeService;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeProjection;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PeriodGradeServiceTest {

    @Mock PeriodGradeRepository periodGradeRepository;
    @Mock AcademicPeriodRepository academicPeriodRepository;
    @Mock TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock PeriodGradeMapper periodGradeMapper;
    @Mock UserClient userClient;

    @InjectMocks PeriodGradeService service;

    @Nested
    @DisplayName("findBySchoolClassId")
    class FindBySchoolClassId {

        @Test
        @DisplayName("возвращает список с именами из user-service")
        void returnsResponsesWithUserData() {
            StudentPeriodGradeProjection projection = new StudentPeriodGradeProjection(1L, 5, "Отлично", 10L);
            UserResponse user = new UserResponse("Иван", "Иванов", "kc-123", 1L);
            StudentPeriodGradeResponse expected = new StudentPeriodGradeResponse(1L, "Иван", "Иванов", 5, "Отлично", 10L);

            when(periodGradeRepository.findPeriodGradesByTeachingAssignment(1L, 3L)).thenReturn(List.of(projection));
            when(userClient.getBatchUsers(List.of(1L))).thenReturn(List.of(user));
            when(periodGradeMapper.toStudentPeriodGradeResponse(projection, user)).thenReturn(expected);

            List<StudentPeriodGradeResponse> result = service.findBySchoolClassId(1L, 3L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(expected);
        }

        @Test
        @DisplayName("пользователь не найден в user-service — маппит с null")
        void userNotFound_mapsWithNull() {
            StudentPeriodGradeProjection projection = new StudentPeriodGradeProjection(99L, null, null, null);
            StudentPeriodGradeResponse expected = new StudentPeriodGradeResponse(99L, null, null, null, null, null);

            when(periodGradeRepository.findPeriodGradesByTeachingAssignment(1L, 3L)).thenReturn(List.of(projection));
            when(userClient.getBatchUsers(List.of(99L))).thenReturn(List.of());
            when(periodGradeMapper.toStudentPeriodGradeResponse(projection, null)).thenReturn(expected);

            List<StudentPeriodGradeResponse> result = service.findBySchoolClassId(1L, 3L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).firstName()).isNull();
        }

        @Test
        @DisplayName("нет оценок в БД — возвращает пустой список")
        void emptyList_returnsEmpty() {
            when(periodGradeRepository.findPeriodGradesByTeachingAssignment(1L, 3L)).thenReturn(List.of());
            when(userClient.getBatchUsers(List.of())).thenReturn(List.of());

            List<StudentPeriodGradeResponse> result = service.findBySchoolClassId(1L, 3L);

            assertThat(result).isEmpty();
            verifyNoInteractions(periodGradeMapper);
        }
    }

    @Nested
    @DisplayName("createGrade")
    class CreateGrade {

        @Test
        @DisplayName("успешно создаёт оценку")
        void success() {
            PeriodGradeRequest dto = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();
            TeachingAssignment ta = TeachingAssignment.builder().build();
            PeriodGrade saved = PeriodGrade.builder().build();
            PeriodGradeResponse expected = new PeriodGradeResponse(1L, 5, "Отлично", 1L);

            when(academicPeriodRepository.findByDate(dto.date())).thenReturn(Optional.of(period));
            when(teachingAssignmentRepository.findById(1L)).thenReturn(Optional.of(ta));
            when(periodGradeRepository.save(any())).thenReturn(saved);
            when(periodGradeMapper.toPeriodGradeResponse(saved)).thenReturn(expected);

            PeriodGradeResponse result = service.createGrade(dto);

            assertThat(result).isEqualTo(expected);
            verify(periodGradeRepository).save(any(PeriodGrade.class));
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void periodNotFound_throwsNotFoundException() {
            PeriodGradeRequest dto = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));

            when(academicPeriodRepository.findByDate(dto.date())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createGrade(dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic period not found");

            verifyNoInteractions(periodGradeRepository);
        }

        @Test
        @DisplayName("период закрыт — бросает ConflictException")
        void periodClosed_throwsConflictException() {
            PeriodGradeRequest dto = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

            when(academicPeriodRepository.findByDate(dto.date())).thenReturn(Optional.of(closedPeriod));

            assertThatThrownBy(() -> service.createGrade(dto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Period is already closed");

            verifyNoInteractions(periodGradeRepository);
        }

        @Test
        @DisplayName("teachingAssignment не найден — бросает NotFoundException")
        void teachingAssignmentNotFound_throwsNotFoundException() {
            PeriodGradeRequest dto = new PeriodGradeRequest(5, "Отлично", 1L, 1L, LocalDate.of(2026, 3, 31));
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();

            when(academicPeriodRepository.findByDate(dto.date())).thenReturn(Optional.of(period));
            when(teachingAssignmentRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createGrade(dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Teaching assignment not found");

            verifyNoInteractions(periodGradeRepository);
        }
    }

    @Nested
    @DisplayName("deletePeriodGrade")
    class DeletePeriodGrade {

        @Test
        @DisplayName("успешно удаляет оценку")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();
            PeriodGrade grade = PeriodGrade.builder().academicPeriod(period).build();

            when(periodGradeRepository.findWithAcademicPeriodById(1L)).thenReturn(Optional.of(grade));

            service.deletePeriodGrade(1L);

            verify(periodGradeRepository).delete(grade);
        }

        @Test
        @DisplayName("оценка не найдена — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(periodGradeRepository.findWithAcademicPeriodById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletePeriodGrade(1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Period grade not found");

            verify(periodGradeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("период закрыт — бросает ConflictException")
        void periodClosed_throwsConflictException() {
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();
            PeriodGrade grade = PeriodGrade.builder().academicPeriod(closedPeriod).build();

            when(periodGradeRepository.findWithAcademicPeriodById(1L)).thenReturn(Optional.of(grade));

            assertThatThrownBy(() -> service.deletePeriodGrade(1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Period is already closed");

            verify(periodGradeRepository, never()).delete(any());
        }
    }
}
