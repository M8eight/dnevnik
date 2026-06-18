package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.AcademicYearMapper;
import com.rusobr.academic.application.service.AcademicYearService;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
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
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private AcademicYearMapper academicYearMapper;

    @InjectMocks
    private AcademicYearService academicYearService;

    private static final Long ACADEMIC_YEAR_ID = 1L;

    // ─────────────────────────────────────────────
    // Builders
    // ─────────────────────────────────────────────

    private AcademicYear buildAcademicYear(boolean isActive) {
        AcademicYear year = new AcademicYear();
        year.setId(ACADEMIC_YEAR_ID);
        year.setName("2024-2025");
        year.setDescription("Description");
        year.setStartDate(LocalDate.of(2024, 9, 1));
        year.setEndDate(LocalDate.of(2025, 5, 31));
        year.setIsActive(isActive);
        return year;
    }

    private AcademicYearResponse buildResponse(boolean isActive) {
        return new AcademicYearResponse(
                ACADEMIC_YEAR_ID,
                "2024-2025",
                "Description",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 5, 31),
                isActive
        );
    }

    private AcademicYearRequest buildRequest() {
        return new AcademicYearRequest(
                "2024-2025",
                "Description",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 5, 31)
        );
    }

    // ─────────────────────────────────────────────
    // getAll
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getAll — возвращает список ответов, отсортированный по дате начала")
    void getAll_ShouldReturnMappedList() {
        AcademicYear entity = buildAcademicYear(true);
        AcademicYearResponse response = buildResponse(true);

        when(academicYearRepository.findAllByOrderByStartDateDesc()).thenReturn(List.of(entity));
        when(academicYearMapper.toResponse(entity)).thenReturn(response);

        List<AcademicYearResponse> result = academicYearService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(ACADEMIC_YEAR_ID);
        verify(academicYearRepository).findAllByOrderByStartDateDesc();
        verify(academicYearMapper).toResponse(entity);
    }

    @Test
    @DisplayName("getAll — возвращает пустой список, если учебных годов нет")
    void getAll_ShouldReturnEmptyList_WhenNoYears() {
        when(academicYearRepository.findAllByOrderByStartDateDesc()).thenReturn(List.of());

        assertThat(academicYearService.getAll()).isEmpty();
    }

    // ─────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findById — возвращает ответ, если учебный год найден")
    void findById_ShouldReturnResponse_WhenFound() {
        AcademicYear entity = buildAcademicYear(true);
        AcademicYearResponse response = buildResponse(true);

        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));
        when(academicYearMapper.toResponse(entity)).thenReturn(response);

        AcademicYearResponse result = academicYearService.findById(ACADEMIC_YEAR_ID);

        assertThat(result.id()).isEqualTo(ACADEMIC_YEAR_ID);
        assertThat(result.name()).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("findById — бросает NotFoundException, если учебный год не найден")
    void findById_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicYearService.findById(ACADEMIC_YEAR_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic year with id " + ACADEMIC_YEAR_ID + " not found");
    }

    // ─────────────────────────────────────────────
    // create
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("create — успешно создаёт и возвращает учебный год")
    void create_ShouldReturnCreatedYear() {
        AcademicYearRequest request = buildRequest();
        AcademicYear entity = buildAcademicYear(true);
        AcademicYearResponse response = buildResponse(true);

        when(academicYearMapper.toEntity(request)).thenReturn(entity);
        when(academicYearRepository.save(entity)).thenReturn(entity);
        when(academicYearMapper.toResponse(entity)).thenReturn(response);

        AcademicYearResponse result = academicYearService.create(request);

        assertThat(result.id()).isEqualTo(ACADEMIC_YEAR_ID);
        verify(academicYearRepository).save(entity);
    }

    @Test
    @DisplayName("create — бросает ConflictException, если startDate после endDate")
    void create_ShouldThrowConflictException_WhenStartDateAfterEndDate() {
        AcademicYearRequest request = new AcademicYearRequest(
                "2024-2025",
                "Description",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2024, 5, 31)
        );

        assertThatThrownBy(() -> academicYearService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start date cannot be after end date");

        verify(academicYearRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // isActive
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("isActive — возвращает true, если учебный год активен")
    void isActive_ShouldReturnTrue_WhenYearIsActive() {
        AcademicYear entity = buildAcademicYear(true);
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));

        boolean result = academicYearService.isActive(ACADEMIC_YEAR_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isActive — бросает NotFoundException, если учебный год не найден")
    void isActive_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicYearService.isActive(ACADEMIC_YEAR_ID))
                .isInstanceOf(NotFoundException.class);
    }

    // ─────────────────────────────────────────────
    // setActive
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("setActive — успешно меняет статус активности")
    void setActive_ShouldChangeStatus_WhenCurrentStatusIsDifferent() {
        AcademicYear entity = buildAcademicYear(true);
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));

        academicYearService.setActive(ACADEMIC_YEAR_ID, false);

        assertThat(entity.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("setActive — бросает ConflictException, если статус уже соответствует запрашиваемому")
    void setActive_ShouldThrowConflictException_WhenStatusIsAlreadySame() {
        AcademicYear entity = buildAcademicYear(true);
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicYearService.setActive(ACADEMIC_YEAR_ID, true))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Academic year is already active");
    }

    // ─────────────────────────────────────────────
    // update
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("update — обновляет переданные поля учебного года")
    void update_ShouldUpdateFieldsAndReturnResponse() {
        AcademicYear entity = buildAcademicYear(true);
        AcademicYearRequest request = new AcademicYearRequest(
                "Updated Name",
                "Updated Description",
                LocalDate.of(2024, 10, 1),
                LocalDate.of(2025, 6, 30)
        );
        AcademicYearResponse response = new AcademicYearResponse(
                ACADEMIC_YEAR_ID, "Updated Name", "Updated Description",
                LocalDate.of(2024, 10, 1), LocalDate.of(2025, 6, 30), true
        );

        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));
        when(academicYearMapper.toResponse(entity)).thenReturn(response);

        AcademicYearResponse result = academicYearService.update(ACADEMIC_YEAR_ID, request);

        assertThat(entity.getName()).isEqualTo("Updated Name");
        assertThat(entity.getDescription()).isEqualTo("Updated Description");
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2024, 10, 1));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));
        assertThat(result.name()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("update — бросает ConflictException, если новые даты невалидны")
    void update_ShouldThrowConflictException_WhenDatesAreInvalid() {
        AcademicYear entity = buildAcademicYear(true);
        AcademicYearRequest request = new AcademicYearRequest(
                null, null,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2025, 1, 1)
        );

        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> academicYearService.update(ACADEMIC_YEAR_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start date cannot be after end date");
    }

    // ─────────────────────────────────────────────
    // delete
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("delete — успешно удаляет учебный год")
    void delete_ShouldDeleteYear() {
        AcademicYear entity = buildAcademicYear(true);
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(entity));

        academicYearService.delete(ACADEMIC_YEAR_ID);

        verify(academicYearRepository).delete(entity);
    }

    @Test
    @DisplayName("delete — бросает NotFoundException, если учебный год для удаления не найден")
    void delete_ShouldThrowNotFoundException_WhenNotFound() {
        when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicYearService.delete(ACADEMIC_YEAR_ID))
                .isInstanceOf(NotFoundException.class);

        verify(academicYearRepository, never()).delete(any());
    }
}