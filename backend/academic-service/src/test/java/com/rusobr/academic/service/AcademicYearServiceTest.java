package com.rusobr.academic.service;

import com.rusobr.academic.application.service.AcademicYearService;
import com.rusobr.academic.application.mapper.AcademicYearMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicYearService tests")
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private AcademicYearMapper academicYearMapper;

    @InjectMocks
    private AcademicYearService academicYearService;

    private AcademicYear testAcademicYear;
    private AcademicYearRequest testRequest;
    private AcademicYearResponse testResponse;

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2023, 9, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 30);

        testAcademicYear = AcademicYear.builder()
                .id(1L)
                .name("2023-2024")
                .description("Academic year 2023-2024")
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .build();

        testRequest = new AcademicYearRequest(
                "2023-2024",
                "Academic year 2023-2024",
                startDate,
                endDate
        );

        testResponse = new AcademicYearResponse(
                1L,
                "2023-2024",
                "Academic year 2023-2024",
                startDate,
                endDate,
                true
        );
    }

    @Nested
    @DisplayName("Get operations")
    class GetOperations {

        @Test
        @DisplayName("Should return all academic years")
        void shouldReturnAllAcademicYears() {
            // Given
            List<AcademicYear> academicYears = List.of(testAcademicYear);
            List<AcademicYearResponse> responses = List.of(testResponse);

            when(academicYearRepository.findAll()).thenReturn(academicYears);
            when(academicYearMapper.toResponse(testAcademicYear)).thenReturn(testResponse);

            // When
            List<AcademicYearResponse> result = academicYearService.getAll();

            // Then
            assertEquals(1, result.size());
            assertEquals(testResponse, result.get(0));
            verify(academicYearRepository, times(1)).findAll();
            verify(academicYearMapper, times(1)).toResponse(testAcademicYear);
        }

        @Test
        @DisplayName("Should return academic year by id")
        void shouldReturnAcademicYearById() {
            // Given
            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testAcademicYear));
            when(academicYearMapper.toResponse(testAcademicYear)).thenReturn(testResponse);

            // When
            AcademicYearResponse result = academicYearService.findById(1L);

            // Then
            assertNotNull(result);
            assertEquals(testResponse, result);
            verify(academicYearRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when academic year not found")
        void shouldThrowNotFoundExceptionWhenAcademicYearNotFound() {
            // Given
            when(academicYearRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NotFoundException.class, () -> academicYearService.findById(999L));
            verify(academicYearRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Create operations")
    class CreateOperations {

        @Test
        @DisplayName("Should create academic year successfully")
        void shouldCreateAcademicYearSuccessfully() {
            // Given
            when(academicYearMapper.toEntity(testRequest)).thenReturn(testAcademicYear);
            when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(testAcademicYear);
            when(academicYearMapper.toResponse(testAcademicYear)).thenReturn(testResponse);

            // When
            AcademicYearResponse result = academicYearService.create(testRequest);

            // Then
            assertNotNull(result);
            assertEquals(testResponse, result);
            verify(academicYearRepository, times(1)).save(any(AcademicYear.class));
        }

        @Test
        @DisplayName("Should throw ConflictException when start date is after end date")
        void shouldThrowConflictExceptionWhenStartDateIsAfterEndDate() {
            // Given
            AcademicYearRequest invalidRequest = new AcademicYearRequest(
                    "2023-2024",
                    "Invalid dates",
                    LocalDate.of(2024, 6, 30),
                    LocalDate.of(2023, 9, 1)
            );

            // When & Then
            assertThrows(ConflictException.class, () -> academicYearService.create(invalidRequest));
            verify(academicYearRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update operations")
    class UpdateOperations {

        @Test
        @DisplayName("Should update academic year successfully")
        void shouldUpdateAcademicYearSuccessfully() {
            // Given
            AcademicYearRequest updateRequest = new AcademicYearRequest(
                    "2024-2025",
                    "Updated description",
                    LocalDate.of(2024, 9, 1),
                    LocalDate.of(2025, 6, 30)
            );

            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testAcademicYear));
            when(academicYearMapper.toResponse(any(AcademicYear.class))).thenReturn(testResponse);

            // When
            AcademicYearResponse result = academicYearService.update(1L, updateRequest);

            // Then
            assertNotNull(result);
            verify(academicYearRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ConflictException when updating with invalid dates")
        void shouldThrowConflictExceptionWhenUpdatingWithInvalidDates() {
            // Given
            AcademicYearRequest invalidRequest = new AcademicYearRequest(
                    "2023-2024",
                    "Invalid dates",
                    LocalDate.of(2024, 6, 30),
                    LocalDate.of(2023, 9, 1)
            );

            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testAcademicYear));

            // When & Then
            assertThrows(ConflictException.class, () -> academicYearService.update(1L, invalidRequest));
        }

        @Test
        @DisplayName("Should throw NotFoundException when updating non-existent academic year")
        void shouldThrowNotFoundExceptionWhenUpdatingNonExistent() {
            // Given
            when(academicYearRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NotFoundException.class, () -> academicYearService.update(999L, testRequest));
        }
    }

    @Nested
    @DisplayName("Delete operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete academic year successfully")
        void shouldDeleteAcademicYearSuccessfully() {
            // Given
            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testAcademicYear));
            doNothing().when(academicYearRepository).delete(testAcademicYear);

            // When
            academicYearService.delete(1L);

            // Then
            verify(academicYearRepository, times(1)).delete(testAcademicYear);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non-existent academic year")
        void shouldThrowNotFoundExceptionWhenDeletingNonExistent() {
            // Given
            when(academicYearRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NotFoundException.class, () -> academicYearService.delete(999L));
            verify(academicYearRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Set active operations")
    class SetActiveOperations {

        @Test
        @DisplayName("Should set academic year as active successfully")
        void shouldSetAcademicYearAsActiveSuccessfully() {
            // Given
            AcademicYear inactiveYear = AcademicYear.builder()
                    .id(1L)
                    .name("2023-2024")
                    .description("Academic year 2023-2024")
                    .startDate(LocalDate.of(2023, 9, 1))
                    .endDate(LocalDate.of(2024, 6, 30))
                    .isActive(false)
                    .build();
            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(inactiveYear));

            // When
            academicYearService.setActive(1L, true);

            // Then
            assertTrue(inactiveYear.getIsActive());
            verify(academicYearRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should set academic year as inactive successfully")
        void shouldSetAcademicYearAsInactiveSuccessfully() {
            // Given
            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testAcademicYear));

            // When
            academicYearService.setActive(1L, false);

            // Then
            assertFalse(testAcademicYear.getIsActive());
        }

        @Test
        @DisplayName("Should throw ConflictException when setting already active year as active")
        void shouldThrowConflictExceptionWhenSettingAlreadyActiveYearAsActive() {
            // Given
            when(academicYearRepository.findById(1L)).thenReturn(Optional.of(testAcademicYear));

            // When & Then
            assertThrows(ConflictException.class, () -> academicYearService.setActive(1L, true));
        }

        @Test
        @DisplayName("Should throw NotFoundException when setting active status on non-existent year")
        void shouldThrowNotFoundExceptionWhenSettingActiveStatusOnNonExistent() {
            // Given
            when(academicYearRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NotFoundException.class, () -> academicYearService.setActive(999L, true));
        }
    }
}
