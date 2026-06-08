package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.application.service.AcademicPeriodService;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.persistence.projection.AcademicPeriodProjection;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicPeriodServiceTest {

    @Mock
    private AcademicPeriodRepository academicPeriodRepository;

    @Mock
    private AcademicPeriodMapper academicPeriodMapper;

    @InjectMocks
    private AcademicPeriodService academicPeriodService;

    private static final Long PERIOD_ID = 1L;

    private AcademicPeriod buildEntity(boolean isClosed) {
        AcademicPeriod period = new AcademicPeriod();
        period.setId(PERIOD_ID);
        period.setName("Q1");
        period.setSchoolYear("2024-2025");
        period.setClosed(isClosed);
        period.setStartDate(LocalDate.of(2024, 9, 1));
        period.setEndDate(LocalDate.of(2024, 12, 31));
        return period;
    }

    private AcademicPeriodResponse buildResponse(boolean isClosed) {
        return new AcademicPeriodResponse(
                PERIOD_ID,
                "Q1",
                "2024-2025",
                isClosed,
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private AcademicPeriodRequest buildRequest() {
        return new AcademicPeriodRequest(
                "Q1",
                "2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    // ─────────────────────────────────────────────
    // getAll
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getAll — возвращает список ответов")
    void getAll_ShouldReturnMappedList() {
        AcademicPeriodProjection projection = mock(AcademicPeriodProjection.class);
        AcademicPeriodResponse response = buildResponse(false);

        when(academicPeriodRepository.findAllOrderAsc()).thenReturn(List.of(projection));
        when(academicPeriodMapper.toResponse(projection)).thenReturn(response);

        List<AcademicPeriodResponse> result = academicPeriodService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(PERIOD_ID);
        verify(academicPeriodRepository).findAllOrderAsc();
        verify(academicPeriodMapper).toResponse(projection);
    }

    @Test
    @DisplayName("getAll — возвращает пустой список если периодов нет")
    void getAll_ShouldReturnEmptyList_WhenNoPeriods() {
        when(academicPeriodRepository.findAllOrderAsc()).thenReturn(List.of());

        assertThat(academicPeriodService.getAll()).isEmpty();
    }

    // ─────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findById — возвращает ответ если период найден")
    void findById_ShouldReturnResponse_WhenFound() {
        AcademicPeriod entity = buildEntity(false);
        AcademicPeriodResponse response = buildResponse(false);

        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));
        when(academicPeriodMapper.toResponse(entity)).thenReturn(response);

        AcademicPeriodResponse result = academicPeriodService.findById(PERIOD_ID);

        assertThat(result.id()).isEqualTo(PERIOD_ID);
        assertThat(result.name()).isEqualTo("Q1");
    }

    @Test
    @DisplayName("findById — бросает NotFoundException если период не найден")
    void findById_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicPeriodService.findById(PERIOD_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic period with id " + PERIOD_ID + " not found");
    }

    // ─────────────────────────────────────────────
    // openPeriod
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("openPeriod — открывает закрытый период")
    void openPeriod_ShouldSetClosedFalse_WhenPeriodIsClosed() {
        AcademicPeriod entity = buildEntity(true);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        academicPeriodService.openPeriod(PERIOD_ID);

        assertThat(entity.isClosed()).isFalse();
    }

    @Test
    @DisplayName("openPeriod — бросает ConflictException если период уже открыт")
    void openPeriod_ShouldThrowConflictException_WhenAlreadyOpen() {
        AcademicPeriod entity = buildEntity(false);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicPeriodService.openPeriod(PERIOD_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Academic period is already open");
    }

    @Test
    @DisplayName("openPeriod — бросает NotFoundException если период не найден")
    void openPeriod_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicPeriodService.openPeriod(PERIOD_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic period with id " + PERIOD_ID + " not found");
    }

    // ─────────────────────────────────────────────
    // closePeriod
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("closePeriod — закрывает открытый период")
    void closePeriod_ShouldSetClosedTrue_WhenPeriodIsOpen() {
        AcademicPeriod entity = buildEntity(false);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        academicPeriodService.closePeriod(PERIOD_ID);

        assertThat(entity.isClosed()).isTrue();
    }

    @Test
    @DisplayName("closePeriod — бросает ConflictException если период уже закрыт")
    void closePeriod_ShouldThrowConflictException_WhenAlreadyClosed() {
        AcademicPeriod entity = buildEntity(true);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicPeriodService.closePeriod(PERIOD_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Academic period is already closed");
    }

    @Test
    @DisplayName("closePeriod — бросает NotFoundException если период не найден")
    void closePeriod_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicPeriodService.closePeriod(PERIOD_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic period with id " + PERIOD_ID + " not found");
    }

    // ─────────────────────────────────────────────
    // create
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("create — создаёт и возвращает период")
    void create_ShouldReturnCreatedPeriod() {
        AcademicPeriodRequest request = buildRequest();
        AcademicPeriod entity = buildEntity(false);
        AcademicPeriodResponse response = buildResponse(false);

        when(academicPeriodMapper.toEntity(request)).thenReturn(entity);
        when(academicPeriodRepository.save(entity)).thenReturn(entity);
        when(academicPeriodMapper.toResponse(entity)).thenReturn(response);

        AcademicPeriodResponse result = academicPeriodService.create(request);

        assertThat(result.id()).isEqualTo(PERIOD_ID);
        assertThat(result.name()).isEqualTo("Q1");
        verify(academicPeriodRepository).save(entity);
    }

    @Test
    @DisplayName("create — бросает ConflictException если startDate после endDate")
    void create_ShouldThrowConflictException_WhenStartDateAfterEndDate() {
        AcademicPeriodRequest request = new AcademicPeriodRequest(
                "Q1",
                "2024-2025",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2024, 9, 1)
        );

        assertThatThrownBy(() -> academicPeriodService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start date cannot be after end date");

        verify(academicPeriodRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // update
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("update — обновляет поля и возвращает ответ")
    void update_ShouldUpdateFieldsAndReturnResponse() {
        AcademicPeriod entity = buildEntity(false);
        AcademicPeriodRequest request = new AcademicPeriodRequest(
                "Q2",
                "2024-2025",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 31)
        );
        AcademicPeriodResponse response = new AcademicPeriodResponse(
                PERIOD_ID, "Q2", "2024-2025", false,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)
        );

        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));
        when(academicPeriodMapper.toResponse(entity)).thenReturn(response);

        AcademicPeriodResponse result = academicPeriodService.update(PERIOD_ID, request);

        assertThat(entity.getName()).isEqualTo("Q2");
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.name()).isEqualTo("Q2");
    }

    @Test
    @DisplayName("update — бросает ConflictException если период закрыт")
    void update_ShouldThrowConflictException_WhenPeriodIsClosed() {
        AcademicPeriod entity = buildEntity(true);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicPeriodService.update(PERIOD_ID, buildRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Academic period is closed");
    }

    @Test
    @DisplayName("update — бросает ConflictException если после обновления startDate после endDate")
    void update_ShouldThrowConflictException_WhenDatesInvalidAfterUpdate() {
        AcademicPeriod entity = buildEntity(false);
        entity.setEndDate(LocalDate.of(2024, 12, 31));

        AcademicPeriodRequest request = new AcademicPeriodRequest(
                null, null,
                LocalDate.of(2025, 6, 1),
                null
        );

        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicPeriodService.update(PERIOD_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start date cannot be after end date");
    }

    @Test
    @DisplayName("update — бросает NotFoundException если период не найден")
    void update_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicPeriodService.update(PERIOD_ID, buildRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic period with id " + PERIOD_ID + " not found");
    }

    // ─────────────────────────────────────────────
    // delete
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("delete — удаляет закрытый период")
    void delete_ShouldDeletePeriod_WhenClosed() {
        AcademicPeriod entity = buildEntity(true);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        academicPeriodService.delete(PERIOD_ID);

        verify(academicPeriodRepository).delete(entity);
    }

    @Test
    @DisplayName("delete — бросает ConflictException если период открыт")
    void delete_ShouldThrowConflictException_WhenPeriodIsOpen() {
        AcademicPeriod entity = buildEntity(false);
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicPeriodService.delete(PERIOD_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete an open academic period");

        verify(academicPeriodRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete — бросает NotFoundException если период не найден")
    void delete_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicPeriodRepository.findById(PERIOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicPeriodService.delete(PERIOD_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic period with id " + PERIOD_ID + " not found");

        verify(academicPeriodRepository, never()).delete(any());
    }
}