package com.rusobr.academic.service;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GradeServiceTest {

    @InjectMocks
    private GradeService gradeService;

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private LessonInstanceRepository lessonInstanceRepository;
    @Mock
    private ScheduleLessonRepository scheduleLessonRepository;

    @Test
    @DisplayName("getGradeById — вернуть DTO если оценка найдена")
    void getGradeById_ShouldReturnDto_WhenGradeExists() {
        Long gradeId = 1L;
        Grade grade = Grade.builder().id(gradeId).studentId(10L).value(5).build();
        GradeResponse dto = new GradeResponse(gradeId, 10L, 5, GradeType.TEST);

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeMapper.toGradeResponseDto(grade)).thenReturn(dto);

        GradeResponse result = gradeService.getGradeById(gradeId);

        assertEquals(dto, result);
        verify(gradeRepository).findById(gradeId);
        verify(gradeMapper).toGradeResponseDto(grade);
    }

    @Test
    @DisplayName("getGradeById — NotFoundException если оценка не найдена")
    void getGradeById_ShouldThrowNotFoundException_WhenGradeNotFound() {
        Long gradeId = 99L;
        when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.getGradeById(gradeId));

        assertTrue(ex.getMessage().contains(String.valueOf(gradeId)));
        verify(gradeRepository).findById(gradeId);
        verifyNoInteractions(gradeMapper);
    }

    @Test
    @DisplayName("createGrade — успешно создать, LessonInstance уже существует")
    void createGrade_ShouldReturnDto_WhenLessonInstanceExists() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 2L, date, 5, GradeType.TEST);

        AcademicPeriod period = AcademicPeriod.builder().isClosed(false).build();
        LessonInstance lessonInstance = LessonInstance.builder().id(10L).lessonDate(date).build();
        Grade savedGrade = Grade.builder().id(100L).studentId(1L).value(5).type(GradeType.TEST).build();
        CreateGradeResponse responseDto = new CreateGradeResponse(1L, 5, GradeType.TEST, 100L, date);

        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(period));
        when(lessonInstanceRepository.findByLessonDateAndScheduleLessonId(date, 2L))
                .thenReturn(Optional.of(lessonInstance));
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);
        when(gradeMapper.toCreateGradeResponseDto(savedGrade, date)).thenReturn(responseDto);

        CreateGradeResponse result = gradeService.createGrade(request);

        assertEquals(responseDto, result);
        verify(academicPeriodRepository).findByDate(date);
        verify(lessonInstanceRepository).findByLessonDateAndScheduleLessonId(date, 2L);
        // ScheduleLesson не нужен — LessonInstance уже есть
        verifyNoInteractions(scheduleLessonRepository);
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    @DisplayName("createGrade — создать LessonInstance если его ещё нет")
    void createGrade_ShouldCreateLessonInstance_WhenNotExists() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 2L, date, 4, GradeType.TEST);

        AcademicPeriod period = AcademicPeriod.builder().isClosed(false).build();
        ScheduleLesson schedule = new ScheduleLesson();
        LessonInstance newInstance = LessonInstance.builder().id(20L).lessonDate(date).scheduleLesson(schedule).build();
        Grade savedGrade = Grade.builder().id(101L).studentId(1L).value(4).type(GradeType.TEST).build();
        CreateGradeResponse responseDto = new CreateGradeResponse(1L, 4, GradeType.TEST, 101L, date);

        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(period));
        when(lessonInstanceRepository.findByLessonDateAndScheduleLessonId(date, 2L)).thenReturn(Optional.empty());
        when(scheduleLessonRepository.findById(2L)).thenReturn(Optional.of(schedule));
        when(lessonInstanceRepository.save(any(LessonInstance.class))).thenReturn(newInstance);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);
        when(gradeMapper.toCreateGradeResponseDto(savedGrade, date)).thenReturn(responseDto);

        CreateGradeResponse result = gradeService.createGrade(request);

        assertEquals(responseDto, result);
        verify(scheduleLessonRepository).findById(2L);
        verify(lessonInstanceRepository).save(any(LessonInstance.class));
    }

    @Test
    @DisplayName("createGrade — NotFoundException если академический период не найден")
    void createGrade_ShouldThrowNotFoundException_WhenPeriodNotFound() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 2L, date, 5, GradeType.TEST);

        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.createGrade(request));

        assertEquals("Academic period not found", ex.getMessage());
        verifyNoInteractions(lessonInstanceRepository, gradeRepository, scheduleLessonRepository);
    }

    @Test
    @DisplayName("createGrade — Conflict если период закрыт")
    void createGrade_ShouldThrowConflict_WhenPeriodIsClosed() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 2L, date, 5, GradeType.TEST);

        AcademicPeriod closedPeriod = AcademicPeriod.builder().isClosed(true).build();
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(closedPeriod));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> gradeService.createGrade(request));

        assertEquals("Academic period is closed", ex.getMessage());
        verifyNoInteractions(lessonInstanceRepository, gradeRepository);
    }

    @Test
    @DisplayName("createGrade — NotFoundException если ScheduleLesson не найден при создании LessonInstance")
    void createGrade_ShouldThrowNotFoundException_WhenScheduleLessonNotFound() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 2L, date, 5, GradeType.TEST);

        AcademicPeriod period = AcademicPeriod.builder().isClosed(false).build();
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(period));
        when(lessonInstanceRepository.findByLessonDateAndScheduleLessonId(date, 2L)).thenReturn(Optional.empty());
        when(scheduleLessonRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.createGrade(request));

        assertEquals("Schedule lesson not found", ex.getMessage());
        verify(lessonInstanceRepository, never()).save(any());
        verify(gradeRepository, never()).save(any());
    }


    @Test
    @DisplayName("deleteGrade — успешно удалить оценку")
    void deleteGrade_ShouldDeleteGrade_WhenValid() {
        Long gradeId = 1L;
        LocalDate date = LocalDate.of(2025, 9, 1);

        LessonInstance lessonInstance = LessonInstance.builder().lessonDate(date).build();
        Grade grade = Grade.builder().id(gradeId).lessonInstance(lessonInstance).build();
        AcademicPeriod period = AcademicPeriod.builder().isClosed(false).build();

        when(gradeRepository.findWithLessonInstanceById(gradeId)).thenReturn(Optional.of(grade));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(period));

        gradeService.deleteGrade(gradeId);

        verify(gradeRepository).delete(grade);
    }

    @Test
    @DisplayName("deleteGrade — NotFoundException если оценка не найдена")
    void deleteGrade_ShouldThrowNotFoundException_WhenGradeNotFound() {
        Long gradeId = 99L;
        when(gradeRepository.findWithLessonInstanceById(gradeId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.deleteGrade(gradeId));

        assertTrue(ex.getMessage().contains(String.valueOf(gradeId)));
        verifyNoInteractions(academicPeriodRepository);
    }

    @Test
    @DisplayName("deleteGrade — Conflict если период закрыт")
    void deleteGrade_ShouldThrowConflict_WhenPeriodIsClosed() {
        Long gradeId = 1L;
        LocalDate date = LocalDate.of(2025, 9, 1);

        LessonInstance lessonInstance = LessonInstance.builder().lessonDate(date).build();
        Grade grade = Grade.builder().id(gradeId).lessonInstance(lessonInstance).build();
        AcademicPeriod closedPeriod = AcademicPeriod.builder().isClosed(true).build();

        when(gradeRepository.findWithLessonInstanceById(gradeId)).thenReturn(Optional.of(grade));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(closedPeriod));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> gradeService.deleteGrade(gradeId));

        assertEquals("Academic period is closed", ex.getMessage());
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteGrade — NotFoundException если период не найден по дате урока")
    void deleteGrade_ShouldThrowNotFoundException_WhenPeriodNotFound() {
        Long gradeId = 1L;
        LocalDate date = LocalDate.of(2025, 9, 1);

        LessonInstance lessonInstance = LessonInstance.builder().lessonDate(date).build();
        Grade grade = Grade.builder().id(gradeId).lessonInstance(lessonInstance).build();

        when(gradeRepository.findWithLessonInstanceById(gradeId)).thenReturn(Optional.of(grade));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.deleteGrade(gradeId));

        assertEquals("Academic period not found", ex.getMessage());
        verify(gradeRepository, never()).delete(any());
    }
}