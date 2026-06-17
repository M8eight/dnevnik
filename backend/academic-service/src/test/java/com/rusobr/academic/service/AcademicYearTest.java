package com.rusobr.academic.service;

import com.rusobr.academic.domain.model.AcademicYear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AcademicYear entity tests")
class AcademicYearTest {

    private AcademicYear academicYear;

    @BeforeEach
    void setUp() {
        academicYear = new AcademicYear();
    }

    @Nested
    @DisplayName("Date validation tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should accept valid start and end dates")
        void shouldAcceptValidDates() {
            // Given
            LocalDate startDate = LocalDate.of(2023, 9, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            // When
            academicYear.setStartDate(startDate);
            academicYear.setEndDate(endDate);
            academicYear.setName("2023-2024");

            // Then - should not throw exception
            assertDoesNotThrow(() -> academicYear.normalize());
            assertEquals(startDate, academicYear.getStartDate());
            assertEquals(endDate, academicYear.getEndDate());
        }

        @Test
        @DisplayName("Should accept when start date equals end date")
        void shouldAcceptWhenStartDateEqualsEndDate() {
            // Given
            LocalDate sameDate = LocalDate.of(2023, 9, 1);
            academicYear.setStartDate(sameDate);
            academicYear.setEndDate(sameDate);
            academicYear.setName("2023-2023");

            // When & Then - same date is allowed (a single day academic year)
            assertDoesNotThrow(() -> academicYear.normalize());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 6, 30);
            LocalDate endDate = LocalDate.of(2023, 9, 1);

            academicYear.setStartDate(startDate);
            academicYear.setEndDate(endDate);
            academicYear.setName("2023-2024");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> academicYear.normalize(),
                    "Should throw IllegalArgumentException when start date is after end date"
            );
            assertEquals("Start date cannot be after end date", exception.getMessage());
        }

        @Test
        @DisplayName("Should allow null dates during normalization")
        void shouldAllowNullDates() {
            // Given
            academicYear.setStartDate(null);
            academicYear.setEndDate(null);
            academicYear.setName("2023-2024");

            // When & Then
            assertDoesNotThrow(() -> academicYear.normalize());
        }

        @Test
        @DisplayName("Should allow partial dates (only start or only end)")
        void shouldAllowPartialDates() {
            // Given - only start date
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(null);
            academicYear.setName("2023-2024");

            // When & Then
            assertDoesNotThrow(() -> academicYear.normalize());

            // Given - only end date
            academicYear = new AcademicYear();
            academicYear.setStartDate(null);
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));
            academicYear.setName("2023-2024");

            // When & Then
            assertDoesNotThrow(() -> academicYear.normalize());
        }
    }

    @Nested
    @DisplayName("Name normalization tests")
    class NameNormalizationTests {

        @Test
        @DisplayName("Should preserve provided name")
        void shouldPreserveProvidedName() {
            // Given
            academicYear.setName("2023-2024");
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When
            academicYear.normalize();

            // Then
            assertEquals("2023-2024", academicYear.getName());
        }

        @Test
        @DisplayName("Should trim whitespace from name")
        void shouldTrimWhitespaceFromName() {
            // Given
            academicYear.setName("  2023-2024  ");
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When
            academicYear.normalize();

            // Then
            assertEquals("2023-2024", academicYear.getName());
        }

        @Test
        @DisplayName("Should generate name from dates when name is null")
        void shouldGenerateNameFromDatesWhenNameIsNull() {
            // Given
            academicYear.setName(null);
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When
            academicYear.normalize();

            // Then
            assertEquals("2023-2024", academicYear.getName());
        }

        @Test
        @DisplayName("Should generate name from dates when name is blank")
        void shouldGenerateNameFromDatesWhenNameIsBlank() {
            // Given
            academicYear.setName("   ");
            academicYear.setStartDate(LocalDate.of(2024, 9, 1));
            academicYear.setEndDate(LocalDate.of(2025, 6, 30));

            // When
            academicYear.normalize();

            // Then
            assertEquals("2024-2025", academicYear.getName());
        }

        @Test
        @DisplayName("Should not generate name when dates are missing")
        void shouldNotGenerateNameWhenDatesAreMissing() {
            // Given
            academicYear.setName(null);
            academicYear.setStartDate(null);
            academicYear.setEndDate(null);

            // When
            academicYear.normalize();

            // Then
            assertNull(academicYear.getName());
        }

        @Test
        @DisplayName("Should not generate name when only start date is present")
        void shouldNotGenerateNameWhenOnlyStartDateIsPresent() {
            // Given
            academicYear.setName(null);
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(null);

            // When
            academicYear.normalize();

            // Then
            assertNull(academicYear.getName());
        }

        @Test
        @DisplayName("Should handle different year formats correctly")
        void shouldHandleDifferentYearFormatsCorrectly() {
            // Given
            academicYear.setName(null);
            academicYear.setStartDate(LocalDate.of(2022, 9, 1));
            academicYear.setEndDate(LocalDate.of(2023, 6, 30));

            // When
            academicYear.normalize();

            // Then
            assertEquals("2022-2023", academicYear.getName());
        }
    }

    @Nested
    @DisplayName("Builder and initialization tests")
    class BuilderAndInitializationTests {

        @Test
        @DisplayName("Should initialize isActive with default value false")
        void shouldInitializeIsActiveWithDefaultValueFalse() {
            // Given & When
            AcademicYear year = AcademicYear.builder()
                    .name("2023-2024")
                    .startDate(LocalDate.of(2023, 9, 1))
                    .endDate(LocalDate.of(2024, 6, 30))
                    .build();

            // Then
            assertFalse(year.getIsActive());
        }

        @Test
        @DisplayName("Should allow setting isActive to true")
        void shouldAllowSettingIsActiveToTrue() {
            // Given & When
            AcademicYear year = AcademicYear.builder()
                    .name("2023-2024")
                    .startDate(LocalDate.of(2023, 9, 1))
                    .endDate(LocalDate.of(2024, 6, 30))
                    .isActive(true)
                    .build();

            // Then
            assertTrue(year.getIsActive());
        }

        @Test
        @DisplayName("Should create academic year with all fields")
        void shouldCreateAcademicYearWithAllFields() {
            // Given & When
            AcademicYear year = AcademicYear.builder()
                    .id(1L)
                    .name("2023-2024")
                    .description("Academic year 2023-2024")
                    .startDate(LocalDate.of(2023, 9, 1))
                    .endDate(LocalDate.of(2024, 6, 30))
                    .isActive(true)
                    .build();

            // Then
            assertEquals(1L, year.getId());
            assertEquals("2023-2024", year.getName());
            assertEquals("Academic year 2023-2024", year.getDescription());
            assertEquals(LocalDate.of(2023, 9, 1), year.getStartDate());
            assertEquals(LocalDate.of(2024, 6, 30), year.getEndDate());
            assertTrue(year.getIsActive());
        }
    }

    @Nested
    @DisplayName("Contains date tests")
    class ContainsDateTests {

        @Test
        @DisplayName("Should return true when date is within academic year")
        void shouldReturnTrueWhenDateIsWithinAcademicYear() {
            // Given
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When & Then
            assertTrue(academicYear.contains(LocalDate.of(2024, 1, 15)));
        }

        @Test
        @DisplayName("Should return true when date equals start date")
        void shouldReturnTrueWhenDateEqualsStartDate() {
            // Given
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When & Then
            assertTrue(academicYear.contains(LocalDate.of(2023, 9, 1)));
        }

        @Test
        @DisplayName("Should return true when date equals end date")
        void shouldReturnTrueWhenDateEqualsEndDate() {
            // Given
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When & Then
            assertTrue(academicYear.contains(LocalDate.of(2024, 6, 30)));
        }

        @Test
        @DisplayName("Should return false when date is before start date")
        void shouldReturnFalseWhenDateIsBeforeStartDate() {
            // Given
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When & Then
            assertFalse(academicYear.contains(LocalDate.of(2023, 8, 31)));
        }

        @Test
        @DisplayName("Should return false when date is after end date")
        void shouldReturnFalseWhenDateIsAfterEndDate() {
            // Given
            academicYear.setStartDate(LocalDate.of(2023, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When & Then
            assertFalse(academicYear.contains(LocalDate.of(2024, 7, 1)));
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString without errors")
        void shouldGenerateToStringWithoutErrors() {
            // Given & When
            AcademicYear year = AcademicYear.builder()
                    .id(1L)
                    .name("2023-2024")
                    .startDate(LocalDate.of(2023, 9, 1))
                    .endDate(LocalDate.of(2024, 6, 30))
                    .build();

            // When & Then
            String result = year.toString();
            assertNotNull(result);
            assertTrue(result.contains("2023-2024"));
        }
    }

    @Nested
    @DisplayName("Validation edge cases")
    class ValidationEdgeCases {

        @Test
        @DisplayName("Should handle leap year dates correctly")
        void shouldHandleLeapYearDatesCorrectly() {
            // Given
            academicYear.setStartDate(LocalDate.of(2024, 2, 29)); // leap year
            academicYear.setEndDate(LocalDate.of(2025, 6, 30));
            academicYear.setName("2024-2025");

            // When & Then
            assertDoesNotThrow(() -> academicYear.normalize());
        }

        @Test
        @DisplayName("Should handle year-long academic years")
        void shouldHandleYearLongAcademicYears() {
            // Given
            academicYear.setStartDate(LocalDate.of(2023, 1, 1));
            academicYear.setEndDate(LocalDate.of(2023, 12, 31));
            academicYear.setName("2023");

            // When & Then
            assertDoesNotThrow(() -> academicYear.normalize());
        }

        @Test
        @DisplayName("Should handle multi-year academic periods")
        void shouldHandleMultiYearAcademicPeriods() {
            // Given
            academicYear.setName(null);
            academicYear.setStartDate(LocalDate.of(2021, 9, 1));
            academicYear.setEndDate(LocalDate.of(2024, 6, 30));

            // When
            academicYear.normalize();

            // Then - should generate name from first and last year
            assertEquals("2021-2024", academicYear.getName());
        }
    }
}
