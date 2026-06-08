package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.FinalGradeMapper;
import com.rusobr.academic.application.service.FinalGradeService;
import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.FinalGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeTeacherResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalGradeServiceTest {

    @Mock private FinalGradeRepository finalGradeRepository;
    @Mock private FinalGradeMapper finalGradeMapper;
    @Mock private TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock private AcademicPeriodRepository academicPeriodRepository;
    @Mock private TeachingAssignmentService teachingAssignmentService;
    @Mock private UserClient userClient;

    @InjectMocks private FinalGradeService service;

    private static final Long STUDENT_ID = 42L;
    private static final Long GRADE_ID = 100L;
    private static final Long ASSIGNMENT_ID = 7L;
    private static final String SCHOOL_YEAR = "2023-2024";

    @Nested
    @DisplayName("getByStudentId")
    class GetByStudentId {

        @Test
        @DisplayName("успешно возвращает мапу итоговых оценок по названию предмета")
        void success() {
            FinalGrade grade = FinalGrade.builder().id(GRADE_ID).build();
            FinalGradeResponse response = new FinalGradeResponse(
                    GRADE_ID, STUDENT_ID, SCHOOL_YEAR, 5, "Отлично", "Математика"
            );

            when(finalGradeRepository.findFinalGradesByStudentId(STUDENT_ID, SCHOOL_YEAR)).thenReturn(List.of(grade));
            when(finalGradeMapper.toFinalGradeResponse(grade)).thenReturn(response);

            Map<String, FinalGradeResponse> result = service.getByStudentId(STUDENT_ID, SCHOOL_YEAR);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result).containsEntry("Математика", response);
            verify(finalGradeRepository).findFinalGradesByStudentId(STUDENT_ID, SCHOOL_YEAR);
        }
    }

    @Nested
    @DisplayName("getByAssignmentId")
    class GetByAssignmentId {

        @Test
        @DisplayName("успешно собирает данные об учениках и их итоговых оценках")
        void success() {
            FinalGrade grade = FinalGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).build();
            FinalGradeResponse response = new FinalGradeResponse(
                    GRADE_ID, STUDENT_ID, SCHOOL_YEAR, 4, "Хорошо", "Математика"
            );
            UserFeignResponse student = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivanov", "key");

            when(finalGradeRepository.findFinalGradesByTeachingAssignmentId(ASSIGNMENT_ID, SCHOOL_YEAR)).thenReturn(List.of(grade));
            when(finalGradeMapper.toFinalGradeResponse(grade)).thenReturn(response);
            when(teachingAssignmentService.getStudentIdsByTeachingAssignmentId(ASSIGNMENT_ID)).thenReturn(List.of(STUDENT_ID));
            when(userClient.getBatchUsers(List.of(STUDENT_ID))).thenReturn(List.of(student));

            List<FinalGradeTeacherResponse> result = service.getByAssignmentId(ASSIGNMENT_ID, SCHOOL_YEAR);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).user()).isEqualTo(student);
            assertThat(result.get(0).finalGrades()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        private final FinalGradeRequest request = new FinalGradeRequest(
                STUDENT_ID, SCHOOL_YEAR, 5, "Отлично", ASSIGNMENT_ID
        );

        @Test
        @DisplayName("успешно создает и сохраняет итоговую оценку")
        void success() {
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();
            TeachingAssignment assignment = TeachingAssignment.builder().id(ASSIGNMENT_ID).build();
            FinalGrade mappedGrade = FinalGrade.builder().build();
            FinalGrade savedGrade = FinalGrade.builder().id(GRADE_ID).teachingAssignment(assignment).build();
            FinalGradeCreateResponse expectedResponse = mock(FinalGradeCreateResponse.class);

            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(List.of(closedPeriod));
            when(teachingAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
            when(finalGradeMapper.toFinalGrade(request)).thenReturn(mappedGrade);
            when(finalGradeRepository.save(mappedGrade)).thenReturn(savedGrade);
            when(finalGradeMapper.toFinalGradeCreateResponse(savedGrade)).thenReturn(expectedResponse);

            FinalGradeCreateResponse result = service.create(request);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(mappedGrade.getTeachingAssignment()).isEqualTo(assignment);
            verify(finalGradeRepository).save(mappedGrade);
        }

        @Test
        @DisplayName("академический период не найден — бросает ConflictException")
        void academicPeriodNotFound_throwsConflictException() {
            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic period not found with school year " + SCHOOL_YEAR);

            verifyNoInteractions(teachingAssignmentRepository, finalGradeRepository);
        }

        @Test
        @DisplayName("академический период не закрыт — бросает ConflictException")
        void academicPeriodNotClosed_throwsConflictException() {
            AcademicPeriod openPeriod = AcademicPeriod.builder().closed(false).build();
            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(List.of(openPeriod));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic period is not closed");

            verifyNoInteractions(teachingAssignmentRepository, finalGradeRepository);
        }

        @Test
        @DisplayName("привязка преподавателя (TeachingAssignment) не найдена — бросает NotFoundException")
        void assignmentNotFound_throwsNotFoundException() {
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();
            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(List.of(closedPeriod));
            when(teachingAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Teaching assignment not found with id " + ASSIGNMENT_ID);

            verifyNoInteractions(finalGradeRepository);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно удаляет итоговую оценку")
        void success() {
            FinalGrade grade = FinalGrade.builder().id(GRADE_ID).schoolYear(SCHOOL_YEAR).build();
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

            when(finalGradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade));
            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(List.of(closedPeriod));

            service.delete(GRADE_ID);

            verify(finalGradeRepository).deleteById(GRADE_ID);
        }

        @Test
        @DisplayName("оценка не найдена — бросает NotFoundException")
        void gradeNotFound_throwsNotFoundException() {
            when(finalGradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(GRADE_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Final grade not found with id " + GRADE_ID);

            verifyNoInteractions(academicPeriodRepository);
            verify(finalGradeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("академический период по году оценки не найден — бросает ConflictException")
        void academicPeriodNotFound_throwsConflictException() {
            FinalGrade grade = FinalGrade.builder().id(GRADE_ID).schoolYear(SCHOOL_YEAR).build();
            when(finalGradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade));
            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.delete(GRADE_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic period not found with school year " + SCHOOL_YEAR);

            verify(finalGradeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("академический период не закрыт — бросает ConflictException")
        void academicPeriodNotClosed_throwsConflictException() {
            FinalGrade grade = FinalGrade.builder().id(GRADE_ID).schoolYear(SCHOOL_YEAR).build();
            AcademicPeriod openPeriod = AcademicPeriod.builder().closed(false).build();

            when(finalGradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade));
            when(academicPeriodRepository.findAcademicPeriodsBySchoolYear(SCHOOL_YEAR)).thenReturn(List.of(openPeriod));

            assertThatThrownBy(() -> service.delete(GRADE_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic period is not closed");

            verify(finalGradeRepository, never()).deleteById(any());
        }
    }
}