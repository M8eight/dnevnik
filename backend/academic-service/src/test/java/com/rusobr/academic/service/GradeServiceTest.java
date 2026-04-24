package com.rusobr.academic.service;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.service.GradeService;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
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
    private LessonInstanceMapper lessonInstanceMapper;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private LessonInstanceRepository lessonInstanceRepository;

    // ─────────────────────────────────────────────
    // getGradeById
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    // createGrade
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("createGrade — успешно создать оценку")
    void createGrade_ShouldReturnDto_WhenValid() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, null, 5, 1, GradeType.TEST);

        LessonInstance lessonInstance = LessonInstance.builder().id(10L).lessonDate(date).build();
        AcademicPeriod period = AcademicPeriod.builder().closed(false).build();
        Grade mappedGrade = Grade.builder().studentId(1L).value(5).type(GradeType.TEST).build();
        Grade savedGrade = Grade.builder().id(100L).studentId(1L).value(5).type(GradeType.TEST).build();
        LessonInstanceDto lessonInstanceDto = new LessonInstanceDto(10L, date);
        CreateGradeResponse responseDto = new CreateGradeResponse(100L, 1L, lessonInstanceDto, 5, 1, GradeType.TEST);

        when(lessonInstanceRepository.findById(10L)).thenReturn(Optional.of(lessonInstance));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(period));
        when(gradeMapper.toGrade(request)).thenReturn(mappedGrade);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);
        when(lessonInstanceMapper.toLessonInstanceDto(lessonInstance)).thenReturn(lessonInstanceDto);
        when(gradeMapper.toCreateGradeResponseDto(savedGrade, lessonInstanceDto)).thenReturn(responseDto);

        CreateGradeResponse result = gradeService.createGrade(request);

        assertEquals(responseDto, result);
        verify(lessonInstanceRepository).findById(10L);
        verify(academicPeriodRepository).findByDate(date);
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    @DisplayName("createGrade — NotFoundException если LessonInstance не найден")
    void createGrade_ShouldThrowNotFoundException_WhenLessonInstanceNotFound() {
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, null, 5, 1, GradeType.TEST);

        when(lessonInstanceRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.createGrade(request));

        assertEquals("Lesson instance not found", ex.getMessage());
        verifyNoInteractions(academicPeriodRepository, gradeRepository, gradeMapper);
    }

    @Test
    @DisplayName("createGrade — NotFoundException если академический период не найден")
    void createGrade_ShouldThrowNotFoundException_WhenPeriodNotFound() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, null, 5, 1, GradeType.TEST);
        LessonInstance lessonInstance = LessonInstance.builder().id(10L).lessonDate(date).build();

        when(lessonInstanceRepository.findById(10L)).thenReturn(Optional.of(lessonInstance));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeService.createGrade(request));

        assertEquals("Academic period not found", ex.getMessage());
        verifyNoInteractions(gradeRepository);
    }

    @Test
    @DisplayName("createGrade — ConflictException если период закрыт")
    void createGrade_ShouldThrowConflict_WhenPeriodIsClosed() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        CreateGradeRequest request = new CreateGradeRequest(1L, 10L, null, 5, 1, GradeType.TEST);
        LessonInstance lessonInstance = LessonInstance.builder().id(10L).lessonDate(date).build();
        AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

        when(lessonInstanceRepository.findById(10L)).thenReturn(Optional.of(lessonInstance));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(closedPeriod));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> gradeService.createGrade(request));

        assertEquals("Academic period is already closed", ex.getMessage());
        verifyNoInteractions(gradeRepository);
    }

    // ─────────────────────────────────────────────
    // deleteGrade
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("deleteGrade — успешно удалить оценку")
    void deleteGrade_ShouldDeleteGrade_WhenValid() {
        Long gradeId = 1L;
        LocalDate date = LocalDate.of(2025, 9, 1);

        LessonInstance lessonInstance = LessonInstance.builder().lessonDate(date).build();
        Grade grade = Grade.builder().id(gradeId).lessonInstance(lessonInstance).build();
        AcademicPeriod period = AcademicPeriod.builder().closed(false).build();

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
    @DisplayName("deleteGrade — NotFoundException если период не найден")
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

    @Test
    @DisplayName("deleteGrade — ConflictException если период закрыт")
    void deleteGrade_ShouldThrowConflict_WhenPeriodIsClosed() {
        Long gradeId = 1L;
        LocalDate date = LocalDate.of(2025, 9, 1);

        LessonInstance lessonInstance = LessonInstance.builder().lessonDate(date).build();
        Grade grade = Grade.builder().id(gradeId).lessonInstance(lessonInstance).build();
        AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

        when(gradeRepository.findWithLessonInstanceById(gradeId)).thenReturn(Optional.of(grade));
        when(academicPeriodRepository.findByDate(date)).thenReturn(Optional.of(closedPeriod));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> gradeService.deleteGrade(gradeId));

        assertEquals("Academic period is closed", ex.getMessage());
        verify(gradeRepository, never()).delete(any());
    }
}